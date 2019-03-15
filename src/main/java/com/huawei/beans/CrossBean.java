package com.huawei.beans;

public class CrossBean {

    //交叉路口的id
    public int crossId;

    public int upRoadId;

    public int rightRoadId;

    public int downRoadId;

    public int leftRoadId;

    public CrossBean(int crossId, int upRoadId, int rightRoadId, int downRoadId, int leftRoadId) {

        this.crossId = crossId;
        this.upRoadId = upRoadId;
        this.rightRoadId = rightRoadId;
        this.downRoadId = downRoadId;
        this.leftRoadId = leftRoadId;
    }
}
