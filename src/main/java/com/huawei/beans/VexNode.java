package com.huawei.beans;

public class VexNode {

    public  int crossId;
    //指向该顶点的入弧
   public  ArcBox firstin;
    //指向该顶点的出弧
    public  ArcBox firstout;

    public  VexNode(int crossId) {
        this.crossId = crossId;
    }

}