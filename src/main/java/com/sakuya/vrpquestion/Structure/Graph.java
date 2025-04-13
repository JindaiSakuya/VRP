package com.sakuya.vrpquestion.Structure;

import com.sakuya.vrpquestion.Strategy.MapLoader;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Graph {
    private MapLoader loader;
    private Map<String, Node> nodes;

    // 双层Map结构保存节点之间的最短路径距离
    // graphMap.get("A").get("B") 表示A到B的距离
    private Map<String, Map<String, Double>> graphMap;

    public Graph(MapLoader loader) {
        this.loader = loader;
        this.nodes = new HashMap<>();
        this.graphMap = new HashMap<>();
    }

    //测试通过
    public void initialize() {
        loader.loadMap(this);
        loader.loadGoods(this);
    }

    public void transformTheMap() {
        for (String from : graphMap.keySet()) {
            Map<String, Double> toMap = graphMap.get(from);
            for (String to : toMap.keySet()) {
                // 自身到自身的距离保持不变
                if (from.equals(to)) continue;

                double originalDistance = toMap.get(to);
                double increment;
                if (from.equals("S") || to.equals("S")) {
                    increment = 50;
                } else {
                    increment = 75;
                }
                toMap.put(to, originalDistance + increment);
            }
        }
    }

    // 获取两个节点之间的距离
    public double getDistance(String from, String to) {
        return graphMap.getOrDefault(from, new HashMap<>()).getOrDefault(to, Double.MAX_VALUE);
    }

    // 判断地图中是否还有未配送完的货物
    public boolean isFinished() {
        return nodes.values().stream().allMatch(Node::isEmpty);
    }

    // 供MapLoader使用
    public void addNode(String name, Node node) {
        nodes.put(name, node);
    }

    // 供MapLoader使用.设置两个节点之间的距离
    public void setDistance(String from, String to, double distance) {
        graphMap.computeIfAbsent(from, k -> new HashMap<>()).put(to, distance);
    }
}
