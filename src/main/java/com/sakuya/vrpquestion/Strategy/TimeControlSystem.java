package com.sakuya.vrpquestion.Strategy;

import com.sakuya.vrpquestion.Structure.DeliveryVehicle;
import lombok.Data;

@Data
public class TimeControlSystem {
    private int days; // 配送天数
    private int currentPeriod; // 当前时间段，1=morning，2=afternoon，3=night
    private double currentBudget; // 当前时间段剩余可行驶路程
    private boolean aheadDelivery; // 是否允许提前配送
    private boolean delayedReturn; // 是否允许滞后返回
    private DeliveryVehicle cart;

    public TimeControlSystem(DeliveryVehicle cart) {
        this.days = 0; //第一天还没开始
        this.currentPeriod = 3; // 初始设定为晚上
        this.currentBudget = -1; //可行驶路程为0
        this.aheadDelivery = false;
        this.delayedReturn = false;
        this.cart=cart;
    }

    // 恢复部分预算
    public void recoverBudget(double additionalBudget) {
        this.currentBudget += additionalBudget;
    }

    // 进入下一个时间段
    public void enterNextPeriod() {
        if (currentPeriod==3) {
            // 从晚上切换到早上
            days++;
            currentPeriod = 1;
            currentBudget = 9000;
            setReady();
        } else if (currentPeriod==1) {
            // 从早上切换到中午
            currentPeriod = 2;
            currentBudget = 9000; // 中午恢复9000的预算
            setReady();
        } else if (currentPeriod==2) {
            // 从中午切换到晚上
            currentPeriod = 3;
            currentBudget = 6000; // 晚上恢复6000的预算
            setReady();
        }
    }

    // 更新节点货物的优先级以及恢复时间利用
    private void setReady() {
        aheadDelivery = true;
        delayedReturn = true;
        cart.getDeliveryMap().getNodes().values().forEach(node -> node.updateGoodsPriority(currentPeriod));
    }
}
