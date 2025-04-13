package com.sakuya.vrpquestion.Structure;

import com.sakuya.vrpquestion.Strategy.*;

public class Client {
    public static void main(String[] args){
        MapLoader mapLoader=new CsvMapLoader("C:\\Users\\KitsuneSakuya\\Desktop\\作业\\算法课VRP问题\\Map_2.csv",
                "C:\\Users\\KitsuneSakuya\\Desktop\\作业\\算法课VRP问题\\package_data_with_time_2.txt");
        Graph map=new Graph(mapLoader);
        map.initialize();
        map.transformTheMap();
        DeliveryVehicle cart=new DeliveryVehicle(map,50,map.getNodes().get("S"));
        NodeJumpStrategy jumpStrategy=new LowStartHighRelayNodeJumpStrategy(cart);
        TimeControlSystem timeControlSystem=new TimeControlSystem(cart);
        cart.setTimeControlSystem(timeControlSystem);
        cart.setNodeJumpStrategy(jumpStrategy);
        cart.operate();
    }
}
