package com.sakuya.vrpquestion.Structure;

import lombok.Data;

@Data
public class Goods implements Comparable<Goods>{
    private double weight;
    private boolean morningAllowed;
    private boolean afternoonAllowed;
    private boolean nightAllowed;
    private int priority;   //从高到低依次是1,2,3,4,5 5表示当前货物不可配送(不在配送时间内)

    public Goods(double weight, boolean morningAllowed, boolean afternoonAllowed, boolean nightAllowed) {
        this.weight = weight;
        this.morningAllowed = morningAllowed;
        this.afternoonAllowed = afternoonAllowed;
        this.nightAllowed = nightAllowed;
        this.priority=5;
    }

    @Override
    public int compareTo(Goods other) {
        // 先按优先级升序（数值小的优先级高）
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // 如果优先级相同，再按重量降序（重的在前）
        return Double.compare(other.weight, this.weight);
    }
}
