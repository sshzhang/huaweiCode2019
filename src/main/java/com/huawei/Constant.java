package com.huawei;

public class Constant {

    /**
     * 表示方位信息
     */
    public static final int UPD = 0;
    public static final int RIGHTD = 1;
    public static final int DOWND = 2;
    public static final int LEFTD = 3;
    /**
     * 表示方向信息
     */
    public static final int LEFT = 4;
    public static final int RIGHT = 5;
    public static final int STRAIGHT = 6;

    //相对djstar中的权重系数
    public static final double W0 = 0.05;
    public static final double W1 = 0.02;
    public static final double W2 = 12;
    // 已废弃
    public static final double W4 = 0;
    // Math.exp(Math.abs((roadBean.speedLimit / carBean.maxSpeed) -1
    public static final double w5 = 7;

    public static  int MAX_CAR_NUMS = 5200 ;
    public static final int ONE_MAX_NUMS = 300;
    // 已修改 @author wxq
    // 统计之后timeLen的时间内启动的预置和优先车辆总数
    public static int TIME_LEN = 50;

    //辅助参数 ---------------------------------------------------------------------------

    // 估计timelen的时间内,可能结束车辆数量,
    // 前期可能会让少量的普通车辆跑, 等全部运行完优先车辆和预置车辆后, 普通车辆的大小为 MAX_CAR_NUMS + FUTURE_END_NUMS
    // 调参时,建议设置为0,即不启动
    public static int FUTURE_END_NUMS = TIME_LEN * 0;
    //如果timelen的时间内时间内,没有优先车辆或者预置车辆启动,则增加道路可上车数量
    //ADD_ALLCARNUMS = 0时,表示关闭该功能
    public static int ADD_ALLCARNUMS = 1800;
    //如果未发车辆数量为 remainCars 时, 扩大地图的车辆容量到 MAX_CAR_NUMS + remainCars
    // remainCars = 0时,表示关闭该功能
    public static int remainCars = 2000;

    //辅助参数 ---------------------------------------------------------------------------

    // 已修改,添加相应开关 @author wxq
    // 是否开启所有的sout,总开关
    public static int IS_ALL_OUT_PRINT = 1;
    // 是否开启System.out.println("time count  MAX_CAR_NUMS finishCarNums allUnStartCarBeanList allStartRunningCarBeanList);
    public static int IS_TIME_NUMS_OUT = 1;
    // 是否开启 System.out.println(priorityEndTime - ReaddingTheData.priorityEarlyPlanTime);
    public static int IS_ALLTIME_OUT = 1;

    // 是否打印某一时间段内,计划顶的预置和优先车辆数目
    public static int IS_PRI_DIRECT_CARS_NUMS_DISTRIBUTE = 0;

    // 已修改  @author wxq
    // 是否统计和输出权重
    public static int IS_PRINT_WEIGHTS = 0;
    // 是否计算当前时间片内, 空车道的数量
    public static int ROAD_SUM_0 = 0;



    //开启死锁回溯
    public static boolean DEAD_LOCK_CHECK_FLAGE = true;
    //程序运行,允许的总死锁次数
    public static int DEAD_COUNT_NUMS = 30;

    //多少个时间片之后重置死锁权重 默认3
    public static int RESET_WEIGHT_TIME_COUNT = 16;
    //死锁时权重的惩罚权重
    public static int PENALTY_WEIGHT = 6;

    //保存前多少个时间片
    public static int MEMORY_TIME_COUNT_NUMS = 12;

    //回溯多少个时间片  TRACK_BACK_TIME_COUNT_NUMS<=MEMORY_TIME_COUNT_NUMS

    public static int TRACK_BACK_TIME_COUNT_NUMS = 12;




}