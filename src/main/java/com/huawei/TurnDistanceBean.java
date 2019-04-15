package com.huawei;

/**
 * 调头所用的Bean
 */
public class TurnDistanceBean {


    public double distance;

    public int roadId;


    public TurnDistanceBean(double distance, int roadId) {

        this.distance = distance;
        this.roadId = roadId;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }








}
