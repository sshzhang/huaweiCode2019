package com.huawei.beans;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 路径bean
 */
public class RoadBean {


    //道路id
    public int roadId;
    //道路长度
    public int roadLength;
    //最高限速
    public int speedLimit;
    //车道数目
    public int roadNums;

    //起始的交叉路口
    public int startCross;

    //终止的交叉路口
    public int  endCross;

    //是否是双向的数据
    public int isBothWay;


    public LinkedList<CarBean>[] carBeanQueues;


    public RoadBean(int roadId, int roadLength, int speedLimit, int roadNums, int  startCross, int  endCross, int isBothWay) {
        this.roadId = roadId;
        this.roadLength = roadLength;
        this.speedLimit = speedLimit;
        this.roadNums = roadNums;
        this.startCross = startCross;
        this.endCross = endCross;
        this.isBothWay = isBothWay;
        carBeanQueues = new LinkedList[roadNums];
    }

    public static void main(String... args) {


    }
}
