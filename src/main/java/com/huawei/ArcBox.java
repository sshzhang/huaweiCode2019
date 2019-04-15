package com.huawei;

public class ArcBox {
    //该弧的尾部所在的位置
    public int tailVex; //表示数组中的索引位置
    //该弧的头部所在位置
    public int headVex;
    //以tailVex为中心 headVex所在的位置信息  0 上 1 右  2 下 3 左
    public int tailDirection;
    //以head为中心 tailVex所在的位置信息
    public int headDirection;
    //弧头相同的节点
    public ArcBox hlink;
    //弧尾相同的节点
    public ArcBox tlink;
    //此条弧的信息
    public RoadBean roadBean;

    //TODO 设置每条边对应的优先队列

    //public LinkedList<CarBean> carBeanPriorityQueues;


    public ArcBox(int tailVex, int headVex, ArcBox hlink, ArcBox tlink, RoadBean roadBean, int tailDirection, int headDirection) {

        this.tailVex = tailVex;
        this.headVex = headVex;
        this.hlink = hlink;
        this.tlink = tlink;
        this.roadBean = roadBean;
        this.tailDirection = tailDirection;
        this.headDirection = headDirection;
        //this.carBeanPriorityQueues = new LinkedList<>();
    }


}