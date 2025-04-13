package com.sakuya.vrpquestion.Strategy;

import com.sakuya.vrpquestion.Structure.Goods;
import com.sakuya.vrpquestion.Structure.Graph;
import com.sakuya.vrpquestion.Structure.Node;

import java.io.*;
import java.util.*;
//测试通过
public class CsvMapLoader implements MapLoader {

    private String mapFilePath;  // 地图文件路径
    private String goodsFilePath;  // 货物文件路径

    public CsvMapLoader(String mapFilePath, String goodsFilePath) {
        this.mapFilePath = mapFilePath;
        this.goodsFilePath = goodsFilePath;
    }

    //测试通过
    @Override
    public void loadMap(Graph graph) {
        try (BufferedReader br = new BufferedReader(new FileReader(mapFilePath))) {
            String line;
            List<String> nodes = new ArrayList<>();
            boolean firstLine = true;

            // 读取地图文件内容
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (firstLine) {
                    // 第一行是节点名称
                    nodes.addAll(Arrays.asList(values).subList(1, values.length)); // 取第一行的节点名称，不包括空的第一个元素
                    firstLine = false;

                    // 初始化每个节点并加入graph
                    GoodsBatchSelectStrategy batchSelect=new PrioritizedGoodsBatchSelectStrategy();
                    for (String nodeName : nodes) {
                        Node node = new Node(nodeName);
                        node.setBatchSelectStrategy(batchSelect);
                        graph.addNode(nodeName, node);  // 向graph中添加节点
                    }
                } else {
                    // 其余行是距离数据
                    String fromNode = values[0];
                    for (int i = 1; i < values.length; i++) {
                        String toNode = nodes.get(i - 1);
                        double distance = Double.parseDouble(values[i]);

                        // 设置节点之间的距离
                        graph.setDistance(fromNode, toNode, distance);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //测试通过
    @Override
    public void loadGoods(Graph graph) {
        Double totalWeight=0.0;//---------------------------------------------------------------------------------------------------------------------------------------------------------
        try (BufferedReader br = new BufferedReader(new FileReader(goodsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String nodeName = parts[0];
                double weight = Double.parseDouble(parts[1].replace("kg", "").trim());

                // 初始化配送时间标志
                boolean morningAllowed = false;
                boolean afternoonAllowed = false;
                boolean nightAllowed = false;

                // 从parts[2]开始检查后续部分
                for (int i = 2; i < parts.length; i++) {
                    String flag = parts[i].trim(); // 可能是空字符串或字符
                    if ("m".equals(flag)) {
                        morningAllowed = true;
                    } else if ("a".equals(flag)) {
                        afternoonAllowed = true;
                    } else if ("n".equals(flag)) {
                        nightAllowed = true;
                    }
                }

                // 创建货物对象
                Goods goods = new Goods(weight, morningAllowed, afternoonAllowed, nightAllowed);
                // 获取对应的节点并添加货物
                Node node = graph.getNodes().get(nodeName);
                if (node != null) {
                    node.addGoods(goods);  // 将货物添加到节点的goodsList中
                    totalWeight+=weight;//---------------------------------------------------------------------------------------------------------------------------------------------------------
                }
            }
            System.out.println("Map Loaded with Cargo Weight: "+totalWeight+" kg in Total"+"\n");//---------------------------------------------------------------------------------------------------------------------------------------------------------
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
