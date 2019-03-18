package com.huawei.beans;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    public int endCross;

    //是否是双向的数据
    public int isBothWay;


    //指向当前队列指针
    public int index = 0;


    public LinkedList<CarBean>[] carBeanQueues;


    public RoadBean(int roadId, int roadLength, int speedLimit, int roadNums, int startCross, int endCross, int isBothWay) {
        this.roadId = roadId;
        this.roadLength = roadLength;
        this.speedLimit = speedLimit;
        this.roadNums = roadNums;
        this.startCross = startCross;
        this.endCross = endCross;
        this.isBothWay = isBothWay;
        carBeanQueues = new LinkedList[roadNums];

        for (int i = 0; i < this.roadNums; i++) {
            carBeanQueues[i] = new LinkedList<CarBean>();
        }

    }


    public static void main(String... args) {

        List<Integer> integers = new ArrayList<>();
        integers.add(12);
        integers.add(23);
        integers.add(34);

        for (int i = 0; i < integers.size(); i++) {
            Integer integer = integers.get(2);
            integers.remove(integer);
            System.out.println(integers.size());
            break;
        }

    }

}
