package com.sakuya.vrpquestion.Strategy;

import com.sakuya.vrpquestion.Structure.Graph;

public interface MapLoader {
    void loadMap(Graph graph);
    void loadGoods(Graph graph);
}
