package com.sakuya.vrpquestion.Strategy;

import com.sakuya.vrpquestion.Structure.DeliveryVehicle;
import com.sakuya.vrpquestion.Structure.Goods;
import com.sakuya.vrpquestion.Structure.Graph;
import com.sakuya.vrpquestion.Structure.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class LowStartHighRelayNodeJumpStrategy implements NodeJumpStrategy {
    private DeliveryVehicle cart;
    //用于审计
    private Double alreadyDelivered=0.0;//----------------------------------------------------------------------------------------------------------------------------------------------------------------
    public LowStartHighRelayNodeJumpStrategy(DeliveryVehicle cart) {
        this.cart = cart;
    }

    @Override
    public void jump() {
        TimeControlSystem timeControlSystem=cart.getTimeControlSystem();
        Graph graph=cart.getDeliveryMap();
        // Step 1: 若预算为负，直接进入下一个时间段
        if (timeControlSystem.getCurrentBudget() < 0) {
            System.out.println("！！！ Budget Not Enough,which is "+timeControlSystem.getCurrentBudget()+",Moving into Next Period ！！！");//----------------------------------------------------------------------------------------------------------------------------------------------------------------
            timeControlSystem.enterNextPeriod();
            System.out.println("⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐"+"Now is Day "+timeControlSystem.getDays()+",Period "+timeControlSystem.getCurrentPeriod()+"⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐");//----------------------------------------------------------------------------------------------------------------------------------------------------------------
        }

        Node currentNode = cart.getCurrentNode();
        List<Node> candidateNodes;
        System.out.println("-------------------"+"Current Node:"+currentNode.getName()+" ,Current Budget: "+timeControlSystem.getCurrentBudget()+",Current Weight:"+(cart.getCurrentWeight())+",Left Weight:"+(cart.getMaxWeight()- cart.getCurrentWeight())+"-------------------");//------------------------------------------------------------------------
        // Step 2: 根据当前节点是否为原点，调用不同的节点获取方法
        boolean isAtStart = currentNode.getName().equals("S");
        if (isAtStart) {
            candidateNodes = this.getLastPrioritizedNodes();
        } else {
            candidateNodes = this.getPrioritizedNodes();
        }
        System.out.println("Candidate Nodes Num:"+candidateNodes.size());//----------------------------------------------------------------------------------------------------------------------------------------------------------------

        Node nextJump = null; // 记录最终选择跳转的节点
        List<Goods> bestGoodsBatchToDeliver = null;
        double bestEfficiency = isAtStart ? 0 : Double.MAX_VALUE;
        int checkedCount = 0;

        for (Node node : candidateNodes) {
            if (checkedCount >= 5) break; // 剪枝：只考察前5个节点
            double consumedReturnBudget;
            double consumedNonReturnBudget;

            if (isAtStart) {
                // 起点时的预算计算
                double distToNode = graph.getDistance("S", node.getName());
                consumedReturnBudget = 2 * distToNode;
                consumedNonReturnBudget = distToNode + 125;
            } else {
                // 非起点时的预算计算（包括回原点和不回原点的预算变化）
                double distToNode = graph.getDistance(currentNode.getName(), node.getName());
                double distNodeToStart = graph.getDistance(node.getName(), "S");
                double distCurrentToStart = graph.getDistance(currentNode.getName(), "S");
                consumedReturnBudget = distToNode + distNodeToStart - distCurrentToStart;
                consumedNonReturnBudget = distToNode - distCurrentToStart + 125;
            }

            // Step 3.2.3: 去都去不了，跳过
            if (consumedNonReturnBudget > timeControlSystem.getCurrentBudget()) continue;

            // Step 3.3: 获取该节点下的最佳货物组合
            List<Goods> currentGoodsBatchToDeliver = node.getBatchSelectStrategy().selectGoodsBatchFrom(node,cart.getMaxWeight()- cart.getCurrentWeight());
            double currentGoodsBatchWeight = currentGoodsBatchToDeliver.stream().mapToDouble(Goods::getWeight).sum();
            System.out.println("Considering: "+node.getName()+" with weight:"+currentGoodsBatchWeight);//--------------------------------------------------------------------------------------------------------------------------------------------
            // Step 3.3.1: 如果只能过去不能回
            if (consumedReturnBudget > timeControlSystem.getCurrentBudget() && consumedNonReturnBudget <= timeControlSystem.getCurrentBudget()) {
                if (timeControlSystem.isDelayedReturn()) {
                    System.out.println(node.getName()+" Supports Delayed Return: ReturnBudget is "+consumedReturnBudget+",But NonReturn Budget is "+consumedNonReturnBudget);//----------------------------------------------------------------------------------------------------------------------------------------------------------------
                    nextJump = node;
                    bestGoodsBatchToDeliver = currentGoodsBatchToDeliver;
                    timeControlSystem.setDelayedReturn(false);
                    break; // 延迟返回策略允许，立即跳转
                } else {
                    continue;
                }
            }

            // Step 3.3.2: 正常去得了也能回
            if (consumedReturnBudget <= timeControlSystem.getCurrentBudget()) {
                // 计算当前组合的效率
                double currentEfficiency = consumedReturnBudget / currentGoodsBatchWeight;

                // 起点时选择效率低的（大），非起点时选择效率高的（小）
                if ((isAtStart && currentEfficiency > bestEfficiency) ||
                        (!isAtStart && currentEfficiency < bestEfficiency)) {
                    nextJump = node;
                    bestGoodsBatchToDeliver = currentGoodsBatchToDeliver;
                    bestEfficiency = currentEfficiency;
                }

                checkedCount++;
            }
        }

// Step 4: 跳转操作或惩罚预算
        if (nextJump != null) {
            if (isAtStart) {
                // 起点场景：跳转成功
                // 扣除预算（返回预算）
                timeControlSystem.recoverBudget(-2 * graph.getDistance(currentNode.getName(), nextJump.getName()));

                // 扣除载重，增加距离
                double deliveredWeight = bestGoodsBatchToDeliver.stream().mapToDouble(Goods::getWeight).sum();
                cart.setCurrentWeight(cart.getCurrentWeight()+deliveredWeight);
                cart.setCurrentDistance(cart.getCurrentDistance()+2 * graph.getDistance(currentNode.getName(), nextJump.getName()));

                // 从目标节点移除已配送货物
                nextJump.deleteGoodsBatch(bestGoodsBatchToDeliver,timeControlSystem.getCurrentPeriod());

                // 更新当前位置
                cart.setCurrentNode(nextJump);
                alreadyDelivered+=deliveredWeight;//----------------------------------------------------------------------------------------------------------------------------------------------------------------
                System.out.println("Heading to "+nextJump.getName()+",with weight:"+deliveredWeight+",consumed Budget: "+(2 * graph.getDistance(currentNode.getName(), nextJump.getName()))+",left Budget: "+timeControlSystem.getCurrentBudget()+",already Delivered: "+alreadyDelivered);//--------------------------------------------------------------------------------------------------------------------------------------------
                // 若允许提前配送，恢复预算
                if (timeControlSystem.isAheadDelivery()) {
                    timeControlSystem.recoverBudget(graph.getDistance(currentNode.getName(), nextJump.getName()) + 125);
                    timeControlSystem.setAheadDelivery(false);
                    System.out.println("Able to deliver ahead of time,recovering Budget: "+graph.getDistance(currentNode.getName(), nextJump.getName()) + 125+",left Budget: "+timeControlSystem.getCurrentBudget());//--------------------------------------------------------------------------------------------------------------------------------------------
                }

            } else {
                // 非起点场景：跳转成功
                // 扣除预算（返回预算）
                timeControlSystem.recoverBudget(-(
                        graph.getDistance(currentNode.getName(), nextJump.getName()) +
                                graph.getDistance(nextJump.getName(), "S") -
                                graph.getDistance(currentNode.getName(), "S")
                        )
                );

                // 扣除载重
                double deliveredWeight = bestGoodsBatchToDeliver.stream().mapToDouble(Goods::getWeight).sum();
                alreadyDelivered+=deliveredWeight;//----------------------------------------------------------------------------------------------------------------------------------------------------------------
                System.out.println("Heading to "+nextJump.getName()+",with weight:"+deliveredWeight+",consumed Budget: "+(graph.getDistance(currentNode.getName(), nextJump.getName()) + graph.getDistance(nextJump.getName(), "S") - graph.getDistance(currentNode.getName(), "S"))+",left Budget: "+timeControlSystem.getCurrentBudget()+",already Delivered: "+alreadyDelivered);//-----------------------------------------------------------------------------------------------------------
                cart.setCurrentWeight(cart.getCurrentWeight()+deliveredWeight);
                cart.setCurrentDistance(cart.getCurrentDistance()+(
                        graph.getDistance(currentNode.getName(), nextJump.getName()) +
                                graph.getDistance(nextJump.getName(), "S") -
                                graph.getDistance(currentNode.getName(), "S")
                ));
                // 从目标节点移除已配送货物
                nextJump.deleteGoodsBatch(bestGoodsBatchToDeliver,timeControlSystem.getCurrentPeriod());

                // 更新当前位置
                cart.setCurrentNode(nextJump);
            }
        } else {
            if (isAtStart) {
                // 起点失败：重惩罚预算
                System.out.println("!!!!! Not Being Able to Head Toward any Nodes,Receiving Heavy Penalty to Force Moving into Next Period !!!!!!");//----------------------------------------------------------------------------------------------------------------------------------------------------------------
                timeControlSystem.recoverBudget(-10000);
            } else {
                // 非起点失败：返回原点并清空载重
                System.out.println("No Available Nodes ,Heading Back");//----------------------------------------------------------------------------------------------------------------------------------------------------------------
                cart.setCurrentNode(graph.getNodes().get("S"));
                cart.setCurrentWeight(0);
                cart.setCurrentDistance(0);
            }
        }
    }


    private List<Node> getPrioritizedNodes() {
        // Step 1: Calculate the current delivery efficiency
        double currentEfficiency = getCurrentEfficiency();
        //System.out.println("??????????????????????????????"+currentEfficiency);

        // Step 2: Collect all nodes except the current node and the origin node
        List<Node> prioritizedNodes = new ArrayList<>();
        Graph graph = cart.getDeliveryMap();
        Node currentNode = cart.getCurrentNode();
        Node originNode = graph.getNodes().get("S");  // Assuming 'S' is the origin node

        // Step 3: Estimate the delivery efficiency for each node
        for (Node node : graph.getNodes().values()) {
            if (node.equals(currentNode) || node.equals(originNode)) {
                continue;  // Skip current node and origin node
            }

            // Calculate estimated added weight
            double estimatedWeight = Math.min(node.getGoodsList().stream().mapToDouble(Goods::getWeight).sum(),
                    cart.getMaxWeight() - cart.getCurrentWeight());

            // Calculate the route increase
            double routeIncrease = graph.getDistance(currentNode.getName(), node.getName()) +
                    graph.getDistance(node.getName(), originNode.getName()) - graph.getDistance(currentNode.getName(), originNode.getName());

            // Calculate the incremental delivery efficiency
            double incrementalEfficiency = routeIncrease / estimatedWeight;

            // Step 4: First pruning: Only consider nodes that improve efficiency
            if (incrementalEfficiency <= currentEfficiency) {
                // Add the node with its calculated efficiency
                node.setIncrementalEfficiency(incrementalEfficiency);  // Store efficiency in the node (custom property)
                prioritizedNodes.add(node);
            }
        }

        // Step 5: Sort the nodes by priority and estimated efficiency
        // Sort by priority (ascending), then by incremental efficiency (ascending)
        // For nodes with same priority, sort by incremental efficiency (ascending)
        prioritizedNodes.sort(Comparator.comparingInt(Node::getPriority).thenComparingDouble(Node::getIncrementalEfficiency));

        // Step 6: Return the nodes, excluding those with priority 5
        List<Node> result = new ArrayList<>();
        for (Node node : prioritizedNodes) {
            if (node.getPriority() != 5) {
                result.add(node);
            }
        }

        return result;
    }

    private List<Node> getLastPrioritizedNodes() {
        // Step 1: Collect all nodes except the current node and the origin node
        List<Node> prioritizedNodes = new ArrayList<>();
        Graph graph = cart.getDeliveryMap();
        Node originNode = graph.getNodes().get("S");  // Assuming 'S' is the origin node

        // Step 2: Add all nodes except origin
        for (Node node : graph.getNodes().values()) {
            if (node.equals(originNode)) {
                continue;  // Skip origin node
            }

            // Step 3: Calculate estimated weight and route increase for the node
            double estimatedWeight = Math.min(node.getGoodsList().stream().mapToDouble(Goods::getWeight).sum(),
                    cart.getMaxWeight() - cart.getCurrentWeight());
            double routeIncrease =graph.getDistance(originNode.getName(), node.getName())*2;
            double incrementalEfficiency = routeIncrease / estimatedWeight;

            // Store the incremental efficiency in the node (or in a temporary structure)
            node.setIncrementalEfficiency(incrementalEfficiency);  // Assuming setIncrementalEfficiency exists

            // Add the node to the list
            prioritizedNodes.add(node);
        }

        // Step 4: Sort the nodes by priority (ascending) and by incremental efficiency (descending)
        prioritizedNodes.sort(Comparator.comparingInt(Node::getPriority).thenComparingDouble((Node n) -> -n.getIncrementalEfficiency()));

        // Step 5: Return the nodes, excluding those with priority 5
        List<Node> result = new ArrayList<>();
        for (Node node : prioritizedNodes) {
            if (node.getPriority() != 5) {
                result.add(node);
            }
        }
        return result;
    }


    private double getCurrentEfficiency() {
        // Calculate the current delivery efficiency (current distance / current weight)
        return cart.getCurrentDistance() / cart.getCurrentWeight();
    }
}
