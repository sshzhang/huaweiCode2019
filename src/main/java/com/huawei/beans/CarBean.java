package com.huawei.beans;

public class CarBean implements Comparable<CarBean> {


    public int carId;
    public int startBean;
    public int endBean;
    //最大速度
    public int maxSpeed;
    //开始时间
    public int startTime;
    //车辆是否已经启动
    public boolean isStart;

    // true 表示在等待  false表示正常结束
    public boolean isWaiting;

    //true表示最终完成
    public boolean isFinish;

    //车辆所在的道路
    public RoadBean currentRoadBean;
    //当前车离最后路径的距离
    public int s1;
    //当前实际速度
    public int currSpeed;
    //访问的地址回溯
    //public int[] preVisted;

    //方向 0 初始启动时刻    1  2  3  分别表示直行 左转 右转
    //public int[] direction;

    //访问过得道路id
    public int[] visitedEdge;

    //已经设置过方向就不需要重新设置方向
    //此字段需要实时更新
    public boolean isSetDirection = false;

    //指向相应的方向数组索引 默认为0
    public int index = 0;

    public CarBean(int carId, int startBean, int endBean, int maxSpeed, int startTime, boolean isStart, int crossLength) {
        this.carId = carId;
        this.startBean = startBean;
        this.endBean = endBean;
        this.maxSpeed = maxSpeed;
        this.startTime = startTime;
        this.isStart = isStart;
        visitedEdge = new int[crossLength];
    }

    //先按时间排序  再按id排序
    @Override
    public int compareTo(CarBean o) {

        int order = this.startTime - o.startTime;
        return order > 0 ? order : order == 0 ? this.carId- this.carId: order;
    }
}
