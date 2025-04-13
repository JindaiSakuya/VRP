package com.sakuya.vrpquestion.Strategy;

import com.sakuya.vrpquestion.Structure.Goods;
import com.sakuya.vrpquestion.Structure.Node;

import java.util.List;

public interface GoodsBatchSelectStrategy {
    List<Goods> selectGoodsBatchFrom(Node node,Double maxWeight);
}
