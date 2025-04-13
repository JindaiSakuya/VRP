package com.sakuya.vrpquestion.Strategy;

import com.sakuya.vrpquestion.Structure.Goods;
import com.sakuya.vrpquestion.Structure.Node;
import org.springframework.stereotype.Component;

import java.util.*;

public class PrioritizedGoodsBatchSelectStrategy implements GoodsBatchSelectStrategy {

    @Override
    public List<Goods> selectGoodsBatchFrom(Node node, Double maxWeight) {
        List<Goods> result = new ArrayList<>();
        int remainingWeight = maxWeight.intValue(); // 直接转换为整数，假设 maxWeight 一定是整数

        // 将货物按优先级（1~4）分组，优先级为5表示暂不可配送，直接跳过
        Map<Integer, List<Goods>> priorityMap = new HashMap<>();
        for (Goods g : node.getGoodsList()) {
            int priority = g.getPriority();
            if (priority < 5) {
                priorityMap.computeIfAbsent(priority, k -> new ArrayList<>()).add(g);
            }
        }

        // 按优先级从高到低依次选择货物组合
        for (int p = 1; p <= 4; p++) {
            List<Goods> goodsList = priorityMap.getOrDefault(p, Collections.emptyList());
            if (goodsList.isEmpty() || remainingWeight <= 0) continue;

            List<Goods> selected = solveKnapsack(goodsList, remainingWeight);
            result.addAll(selected);

            // 更新剩余载重
            int used = selected.stream().mapToInt(g -> (int) (g.getWeight())).sum();
            remainingWeight -= used;
        }

        return result;
    }

    /**
     * 使用 0-1 背包算法选择不超过 maxWeight 的最大总重量货物组合。
     */
    private List<Goods> solveKnapsack(List<Goods> goodsList, int maxWeight) {
        int n = goodsList.size();

        // 初始化 dp 数组，记录每个容量下的最大值
        int[] dp = new int[maxWeight + 1];
        boolean[][] pick = new boolean[n + 1][maxWeight + 1];

        // 动态规划填充 dp 数组
        for (int i = 1; i <= n; i++) {
            int weight = (int) (goodsList.get(i - 1).getWeight()); // 货物重量转换为整数
            for (int w = maxWeight; w >= weight; w--) {
                if (dp[w] < dp[w - weight] + weight) {
                    dp[w] = dp[w - weight] + weight;
                    pick[i][w] = true;
                }
            }
        }

        // 回溯选中的货物
        List<Goods> selected = new ArrayList<>();
        int w = maxWeight;
        for (int i = n; i >= 1; i--) {
            if (pick[i][w]) {
                Goods g = goodsList.get(i - 1);
                selected.add(g);
                w -= (int) (g.getWeight()); // 更新剩余重量
            }
        }

        // 返回选中的货物
        Collections.reverse(selected); // 选中的货物需要按顺序返回
        return selected;
    }
}