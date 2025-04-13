package com.sakuya.vrpquestion.Structure;

import com.sakuya.vrpquestion.Strategy.NodeJumpStrategy;
import com.sakuya.vrpquestion.Strategy.TimeControlSystem;
import lombok.Data;

@Data
public class DeliveryVehicle {
    private Graph deliveryMap; // 送货地图
    private double maxWeight; // 小货车最大载重
    private double currentWeight; // 当前载重
    private double currentDistance; // 当前行驶距离
    private Node currentNode; // 当前所在节点
    private NodeJumpStrategy nodeJumpStrategy; // 跳转策略
    private TimeControlSystem timeControlSystem; // 时间控制系统

    public DeliveryVehicle(Graph deliveryMap, double maxWeight, Node startNode) {
        this.deliveryMap = deliveryMap;
        this.maxWeight = maxWeight;
        this.currentWeight = 0;
        this.currentDistance = 0;
        this.currentNode = startNode;
    }

    // 运作方法，执行送货任务
    public void operate() {
        while(!this.getDeliveryMap().isFinished()){
            this.nodeJumpStrategy.jump();
        }
        System.out.println("\n"+"All Finished! "+"Days Spent: "+timeControlSystem.getDays());
    }
}
