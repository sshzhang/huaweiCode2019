package com.huawei.beans;

import java.util.LinkedList;

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

    public boolean visible = true;

    //指向当前队列指针
    public int index = 0;


    public LinkedList<CarBean>[] carBeanQueues;


    public RoadBean(int roadId, int roadLength, int speedLimit, int roadNums, int startCross, int endCross, int isBothWay, boolean visible) {
        this.roadId = roadId;
        this.roadLength = roadLength;
        this.speedLimit = speedLimit;
        this.roadNums = roadNums;
        this.startCross = startCross;
        this.endCross = endCross;
        this.isBothWay = isBothWay;
        this.visible = visible;
        this.carBeanQueues = new LinkedList[roadNums];
        for (int i = 0; i < this.roadNums; i++) {
            carBeanQueues[i] = new LinkedList<CarBean>();
        }

    }


    public static void main(String... args) {

      /*  LinkedList<Integer> integers = new LinkedList<>();
        integers.add(12);
        integers.add(23);
        integers.add(34);

        for (int i = 0; i < integers.size(); i++) {

            System.out.println(integers.poll());
            System.out.println(integers.size());
            i--;
        }*/

        assert 1 != 2;


        LinkedList[] links = new LinkedList[5];

        for (int i = 0; i < links.length; i++) {
            links[i] = new LinkedList<Integer>();
        }
        links[0].add(1);
        links[0].add(3);

        Test(links[0]);
        System.out.println(links);






    }


    public static void Test(LinkedList<Integer> params) {

        params.poll();

        params.add(12);
    }
}
