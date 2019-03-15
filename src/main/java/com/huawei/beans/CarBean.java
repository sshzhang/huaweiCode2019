package com.huawei.beans;

public class CarBean implements  Comparable<CarBean>{


    public int carId;
    public int startBean;
    public int endBean;
    //最大速度
    public int maxSpeed;
    //开始时间
    public int startTime;
    //车辆是否已经启动
    public boolean isStart;

    public boolean isFinish;
    //车辆所在的道路
    public RoadBean currentRoadBean;
    //当前车离最后路径的距离
    public int d1;
    //当前实际速度
    public int currSpeed;
    //访问的地址回溯
    public int[] preVisted;
    //转弯时候的方向  0 1 2 3
    public int direction = -1;


    public CarBean(int carId, int startBean, int endBean, int maxSpeed, int startTime, boolean isStart) {
        this.carId = carId;
        this.startBean = startBean;
        this.endBean = endBean;
        this.maxSpeed = maxSpeed;
        this.startTime = startTime;
        this.isStart = isStart;
    }

    @Override
    public int compareTo(CarBean o) {
        return this.carId - o.carId;
    }
}
