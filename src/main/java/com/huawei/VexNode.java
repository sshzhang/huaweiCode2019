package com.huawei;

public class VexNode {

    public  int crossId;
    //指向该顶点的入弧
   public ArcBox firstin;
    //指向该顶点的出弧
    public ArcBox firstout;
    //设置当前交叉口的拥堵情况, 默认是0表示不拥堵
    //当前只设置找不到最佳方向时候,判断拥堵情况来实现路径选择
    public double  crossBusiness = 0;
    public  VexNode(int crossId) {
        this.crossId = crossId;
    }
}