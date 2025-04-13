package com.sakuya.vrpquestion.Structure;

import com.sakuya.vrpquestion.Strategy.GoodsBatchSelectStrategy;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Data
public class Node {
    private String name;
    private List<Goods> goodsList;
    private double morningDelivery;
    private double afternoonDelivery;
    private double nightDelivery;
    private double incrementalEfficiency;
    private GoodsBatchSelectStrategy batchSelectStrategy;

    public Node(String name) {
        this.name = name;
        this.goodsList = new ArrayList<>();
        this.morningDelivery = 0;
        this.afternoonDelivery = 0;
        this.nightDelivery = 0;
    }

    public void addGoods(Goods goods) {
        goodsList.add(goods);
        updateDeliveryWeight(goods, true);
    }

    public void deleteGoods(Goods goods) {
        goodsList.remove(goods);
        updateDeliveryWeight(goods, false);
    }

    public void deleteGoodsBatch(List<Goods> toRemove,int currentPeriod) {
        for (Goods goods : toRemove) {
            deleteGoods(goods);
        }
        updateGoodsPriority(currentPeriod);
    }

    public boolean isEmpty() {
        return goodsList.isEmpty();
    }

    public void sortGoodsList() {
        Collections.sort(goodsList);
    }

    public int getPriority() {
        return goodsList.isEmpty() ? 5 : goodsList.get(0).getPriority(); // 空则为最低优先级
    }

    /**
     * 更新该节点下所有货物的优先级。
     * @param currentPeriod 当前时间段，1=morning，2=afternoon，3=night
     */
    public void updateGoodsPriority(int currentPeriod) {
        // 确定除了当前时间段外，哪个时间段的配送压力更大
        int prioritizedPeriod = -1;
        if (currentPeriod == 1) { // 现在是早上，比较下午和晚上（夜晚乘1.5作为加权）
            prioritizedPeriod = (afternoonDelivery >= nightDelivery * 1.5) ? 2 : 3;
        } else if (currentPeriod == 2) { // 现在是下午，比较早上和晚上
            prioritizedPeriod = (morningDelivery >= nightDelivery * 1.5) ? 1 : 3;
        } else if (currentPeriod == 3) { // 现在是晚上，比较早上和下午
            prioritizedPeriod = (morningDelivery >= afternoonDelivery) ? 1 : 2;
        }

        for (Goods goods : goodsList) {
            boolean morning = goods.isMorningAllowed();
            boolean afternoon = goods.isAfternoonAllowed();
            boolean night = goods.isNightAllowed();

            // 当前时间段是否允许配送
            boolean allowedNow = switch (currentPeriod) {
                case 1 -> morning;
                case 2 -> afternoon;
                case 3 -> night;
                default -> false;
            };

            if (!allowedNow) {
                goods.setPriority(5); // 当前时间段不可配送
                continue;
            }

            int allowedCount = (morning ? 1 : 0) + (afternoon ? 1 : 0) + (night ? 1 : 0);

            if (allowedCount == 1) {
                goods.setPriority(1); // 只能在当前时间段配送
            } else if (allowedCount == 3) {
                goods.setPriority(4); // 所有时间段都可以
            } else {
                // 允许当前 + 一个其他时间段，判断这个其他时间段是否为 prioritizedPeriod
                boolean supportsPrioritized = switch (prioritizedPeriod) {
                    case 1 -> morning;
                    case 2 -> afternoon;
                    case 3 -> night;
                    default -> false;
                };

                // 如果该货物也支持压力更大的那个时间段 → 当前应优先送掉它 → 优先级设为2
                // 否则 → 优先级设为3
                goods.setPriority(supportsPrioritized ? 2 : 3);
            }
        }

        sortGoodsList(); // 更新后排序
    }

    private void updateDeliveryWeight(Goods goods, boolean isAdd) {
        double delta = isAdd ? goods.getWeight() : -goods.getWeight();
        if (goods.isMorningAllowed()) {
            morningDelivery += delta;
        }
        if (goods.isAfternoonAllowed()) {
            afternoonDelivery += delta;
        }
        if (goods.isNightAllowed()) {
            nightDelivery += delta;
        }
    }
}

