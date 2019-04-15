package com.huawei;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 包含所有的调度逻辑
 */
public class ArrangeTheCarForEnd {
    public static int time = 0;
    public static int count = 0;


    //优先车辆的个数
    public static int priorityCarNum = 0;

    public static int priorityCarMaxSpeed = 0;

    public static int priorityCarMinSpeed = Integer.MAX_VALUE;

    public static int allCarMaxSpeed = 0;
    public static int allCarMinSpeed = Integer.MAX_VALUE;
    public static int priorityCarEarlyTime = Integer.MAX_VALUE;
    public static int priorityCarLateTime = 0;
    public static int allCarMaxEarlyTime = Integer.MAX_VALUE;

    public static int allCarMinLateTime = 0;

    public static int allCarStartCross = 0;
    public static int allCarEndCross = 0;
    public static int priorityCarStartCross = 0;
    public static int priorityCarEndCross = 0;

    public static int priorityEndTime = -1;
    private static VexNode[] Graph = null;

    public static int finishCarNums = 0;

    private static int lastWaiting = -1;

    //死锁次数,超过一次直接报死锁
    private static int deadNumCount = 0;
    private static List<CrossBean> crossBeanList;
    private static List<RoadBean> roadBeanList;
    private static List<CarBean> carBeanList;
    //所有没有开启的车
    public static List<CarBean> allUnStartCarBeanList;
    //所有正在路上运行车辆
    public static List<CarBean> allStartRunningCarBeanList = new ArrayList<>();
    //所有完成运行的车辆
    public static List<CarBean> allFinishedCarBeanList = new ArrayList<>();


    //  备份上一个时间片所有运行的车辆, 每一个时间片清空一次
   // public static List<CarBean> backUpLastCarBeanList = new ArrayList<>();
    //key 表示上一个时间片备份上一个时间片, 车辆运行车辆的集合  TreeMap默认按键升序排序
    public static TreeMap<Integer, List<CarBean>> backUpLastTimeStartRunningCarBeanList = new TreeMap<>();
    //key+1 表示当前时间片   当前时间片, 车辆变为终止状态的集合
    public static TreeMap<Integer, List<CarBean>> backUpCurrentNoWaitingCarBeanList = new TreeMap<>();
    //key+1表示当前时间片  当前时间片, 车辆变为结束状态的集合
    public static TreeMap<Integer, List<CarBean>> backUpCurrentEndCarBeanList = new TreeMap<>();

    //当前时间片终止的车辆
    public static List<CarBean> currNoWaitingCarBeanList = null;
    //当前时间片结束的车辆
    public static List<CarBean> currEndCarBeanList = null;






    //
    public static int planLength = 5000;
    // 当前某一时间,计划启动的预置和优先车辆数目
    public static int[] planStartNums = new int[planLength];
    // 某一时间段内,计划启动的预置和优先车辆数目
    public static int[] futureStartNums = new int[planLength];
    // 统计之后timeLen的时间内启动的预置和优先车辆总数
    public static int timeLen = Constant.TIME_LEN;

    // 已修改,  用作统计权重 @author wxq
    public static int tempTime = 0;
    //统计 空道路的数量
    public static int zeroCapcityNums = 0;
    // 保存当前时间片内所有的道路的长度信息
    public static ArrayList<Integer> roadlengths = new ArrayList<>(1000);
    // 保存当前时间片内所有道路的最大速度的信息
    public static ArrayList<Integer> speedLimits = new ArrayList<>(1000);
    // 保存当前时间片内 sum / roadBean.roadNums 的信息
    public static ArrayList<Double>  capacitys = new ArrayList<>(1000);
    // 保存当前时间片内 车道速度与当前车辆实际车速的比重
    public static ArrayList<Double>  w5Values = new ArrayList<>(1000);

    //保存所有实时等待状态车辆集合
    //private static List<CarBean> dynamicWaitingCarBeanList = new ArrayList<>();

    //保存实时终止状态的车辆
    //private  static List<CarBean> dynamicNoWaitingCarBeanList = new ArrayList<>();


    public static void ArrangeTheCar(List<CrossBean> crossBeanList, List<RoadBean> roadBeanList, List<CarBean> carBeanList, String answerPath) {

        ArrangeTheCarForEnd.crossBeanList = crossBeanList;
        ArrangeTheCarForEnd.roadBeanList = roadBeanList;
        ArrangeTheCarForEnd.carBeanList = carBeanList;
        ArrangeTheCarForEnd.allUnStartCarBeanList = new ArrayList<CarBean>(carBeanList);
        BuildTheVertecNodesMap();
        for (Map.Entry<Integer, Integer> entry : ReaddingTheData.priorityStartCross.entrySet()) {
            if (entry.getValue() == 1) {
                priorityCarStartCross++;
            }
        }


        for (Map.Entry<Integer, Integer> entry : ReaddingTheData.priorityEndCross.entrySet()) {
            if (entry.getValue() == 1) {
                priorityCarEndCross++;
            }
        }


        for (Map.Entry<Integer, Integer> entry : ReaddingTheData.StartCross.entrySet()) {
            if (entry.getValue() == 1) {
                allCarStartCross++;
            }
        }


        for (Map.Entry<Integer, Integer> entry : ReaddingTheData.EndCross.entrySet()) {

            if (entry.getValue() == 1) {
                allCarEndCross++;
            }
        }

        double a = carBeanList.size() * 0.05 / priorityCarNum + (allCarMaxSpeed * 0.2375 / allCarMinSpeed) / (priorityCarMaxSpeed * 1.0 / priorityCarMinSpeed) + (allCarMinLateTime * 0.2375 / allCarMaxEarlyTime) / (priorityCarLateTime * 1.0 / priorityCarEarlyTime) + (priorityCarStartCross == 0 ? 0 : allCarStartCross * 0.2375 / priorityCarStartCross) + (priorityCarEndCross == 0 ? 0 : allCarEndCross * 0.2375 / priorityCarEndCross);

        if (Constant.IS_ALL_OUT_PRINT == 1)
            System.out.println(a);

        // 初始化
        Arrays.fill(planStartNums, 0);
        // 计算某一时间,计划启动的预置和优先车辆数目
        for (CarBean carBean : carBeanList) {
            if(carBean.priorityCar == 1 || carBean.directionCar == 1){
                if (carBean.startTime < planLength){
                    planStartNums[carBean.startTime]++;
                }else {
                    planStartNums[planLength-1]++;
                }
            }
        }
        // 计算某一时间段内,计划启动的预置和优先车辆数目
        int tempNums = 0;
        for (int i = planStartNums.length-1 ; i >= 0; i--) {
            if((planStartNums.length-1 - i) < timeLen){
                tempNums += planStartNums[i];
                futureStartNums[i] = tempNums;
            }else {
                tempNums = tempNums - planStartNums[i+timeLen] + planStartNums[i];
                futureStartNums[i] = tempNums;
            }
        }

        // 打印某一时间段内,计划顶的预置和优先车辆数目
        if(Constant.IS_ALL_OUT_PRINT == 1 && Constant.IS_PRI_DIRECT_CARS_NUMS_DISTRIBUTE == 1){
            System.out.print("Method: ArrangeTheCar ->  timeLen: " + timeLen + " futureStartNums: " );
            for (int i = 0; i < futureStartNums.length; i++) {
                System.out.print(" {" + " i: " + i + " futureStartNums: " + futureStartNums[i] + "} ");
            }
            System.out.println();
        }


        //统计死锁多少次之后权重归为0
        int countTheTimeOfAfterDeadLock = 0;

        while (!isFinish()) {


            //保存上一个时间片上三个集合信息,记住是深拷贝
            //MemoryTheLastTimeCarBeanList();
            currNoWaitingCarBeanList = new ArrayList<>();
            currEndCarBeanList = new ArrayList<>();

            //清空上一个时间片的所有车辆
            //增加输出开关
            if(Constant.IS_ALL_OUT_PRINT == 1 && Constant.IS_TIME_NUMS_OUT == 1){
                System.out.println("time: " + time + " MAX_CAR_NUMS: " + Constant.MAX_CAR_NUMS +
                        " finishCarNums: " + allFinishedCarBeanList.size() + " allUnStartCarBeanList: " +
                        allUnStartCarBeanList.size() + " allStartRunningCarBeanList: " + allStartRunningCarBeanList.size()
                        + " futureStartNums: " + futureStartNums[time < planLength ? time : planLength-1]);
            }
            //标注车辆的状态信息 终止或者等待
            FlageTheWaitingCar(null, -1);
            //drivePreSetCarInGarage(time);
            //开启优先车辆
            driverAllCarInGarage(true, time, null);
            //创建优先队列
            createCarSequence();
            if (!driverCarInWaitState(time)){
                countTheTimeOfAfterDeadLock = 0;
                if (!Constant.DEAD_LOCK_CHECK_FLAGE || deadNumCount >= Constant.DEAD_COUNT_NUMS) { // 连续死锁次数大于Constant.deadNums
                    throw new RuntimeException("死锁 deadNumCount:" + deadNumCount + "  Constant.deadNums: " + Constant.DEAD_COUNT_NUMS);
                } else {//回溯所有车辆的状态信息
                    /**
                     * 找出出现死锁的车所在的道路 , 增加惩罚项, 从而权重增大
                     *   回溯所有车辆的状态
                     *   a)设置死锁车辆所在权重变大
                     *   b)清空所有道路上的所有车
                     *   c)对备份车辆数据进行排序
                     *   d)恢复道路的状态到上一个时间片
                     *   time--
                     */
                    if (Constant.IS_ALL_OUT_PRINT == 1) {
                        System.out.println("--------------------警告-------------------");
                        System.out.println("***************回溯*************************");
                        System.out.println("--------------------警告-------------------");
                    }
                    //设置数据
                    backUpCurrentEndCarBeanList.put(time, currEndCarBeanList);
                    backUpCurrentNoWaitingCarBeanList.put(time, currNoWaitingCarBeanList);
                    //死锁的次数+1
                    deadNumCount++;
                    //先直接回溯到  备份数据的最前方 Constant.beforeTimeCountNums
                    //设置数据到地图数据中
                    //设置相应权重信息

                    int dirctionCarNums = 0, priorityCarNums=0, commonCarNums=0;

                    for (CarBean carBean : allStartRunningCarBeanList) {
                        if (carBean.isWaiting) {
                            carBean.currentRoadBean.penaltyWeight += Constant.PENALTY_WEIGHT;
                            //直接设置为false, 本质上此时间片,此车辆没有走。

                            commonCarNums++;
                            dirctionCarNums = carBean.directionCar==1 ? dirctionCarNums + 1 : dirctionCarNums;
                            priorityCarNums = carBean.priorityCar==1 ? priorityCarNums+ 1 : priorityCarNums;
                            carBean.isWaiting = false;
                            //if (Constant.IS_ALL_OUT_PRINT == 1)
                            //System.out.println(carBean.carId + "  " + carBean.currentRoadBean.roadId + " " + carBean.currentRoadBean.penaltyWeight);
                        }
                    }
                    if (Constant.IS_ALL_OUT_PRINT == 1)
                        System.out.println("priorityCarNums: "+ priorityCarNums+" dirctionCarNums: "+dirctionCarNums+" commonCarNums: "+commonCarNums);


                    /**
                     * 状态回溯
                     *
                     */
                    //回溯前几个时间片
                    int trackBackNums = Math.min(Constant.TRACK_BACK_TIME_COUNT_NUMS, backUpCurrentNoWaitingCarBeanList.size());
                    //开始回溯
                    if (trackBackNums>0) {
                        Iterator<Map.Entry<Integer, List<CarBean>>> NoWaitingCarBeanListEntries = backUpCurrentNoWaitingCarBeanList.descendingMap().entrySet().iterator();
                        Iterator<Map.Entry<Integer, List<CarBean>>> EndCarBeanListEntries = backUpCurrentEndCarBeanList.descendingMap().entrySet().iterator();
                        // Iterator<Map.Entry<Integer, List<CarBean>>> StartRunningCarBeanListEntries = backUpLastTimeStartRunningCarBeanList.descendingMap().entrySet().iterator();
                        while (NoWaitingCarBeanListEntries.hasNext()) {
                            if (!EndCarBeanListEntries.hasNext()) throw new RuntimeException("集合类异常");
                            //if (!StartRunningCarBeanListEntries.hasNext()) throw new RuntimeException("集合类异常");
                            //当前终止状态集合   终止状态车辆的回退
                            Map.Entry<Integer, List<CarBean>> NoWaitingCarBeanListEntriesNext = NoWaitingCarBeanListEntries.next();
                            List<CarBean> currNoWaitingCarBeanList = NoWaitingCarBeanListEntriesNext.getValue();

                            for (CarBean carBean : currNoWaitingCarBeanList) {
                                CarBean currBean = null;
                                for (CarBean carinlist : carBeanList) {
                                    if (carinlist.carId == carBean.carId) {
                                        currBean = carinlist;
                                        break;
                                    }
                                }
                                if (currBean == null) throw new RuntimeException("数据格式异常");

                                if (carBean.isStart) {//这辆车已经开启   开始等待---->开始终止
                                    carBean.isWaiting = false;
                                    //把车辆的状态信息更新
                                    currBean.CopyALLValue(carBean);
                                } else {//这辆车未开启   回溯 未开启------->开始终止
                                    if (!allStartRunningCarBeanList.remove(currBean))
                                        throw new RuntimeException("数据格式异常");
                                    if (!allUnStartCarBeanList.add(currBean)) throw new RuntimeException("数据格式异常");
                                    if (carBean.isWaiting) throw new RuntimeException("数据格式异常");
                                    currBean.CopyALLValue(carBean);
                                }
                            }

                            //移除数据
                            NoWaitingCarBeanListEntries.remove();

                            //if(backUpCurrentNoWaitingCarBeanList.remove(NoWaitingCarBeanListEntriesNext.getKey())==null)  throw new RuntimeException("数据格式异常");

                            //结束状态车辆的回退
                            Map.Entry<Integer, List<CarBean>> EndCarBeanListEntriesNext = EndCarBeanListEntries.next();
                            List<CarBean> currEndCarBeanList = EndCarBeanListEntriesNext.getValue();
                            for (CarBean carBean : currEndCarBeanList) {
                                CarBean currBean = null;
                                for (CarBean carinlist : carBeanList) {
                                    if (carinlist.carId == carBean.carId) {
                                        currBean = carinlist;
                                        break;
                                    }
                                }
                                if (currBean == null || !carBean.isStart) throw new RuntimeException("数据格式异常");


                                if (!allFinishedCarBeanList.remove(currBean)) throw new RuntimeException("数据格式异常");

                                if (!allStartRunningCarBeanList.add(currBean)) throw new RuntimeException("数据格式异常");
                                carBean.isWaiting = false;
                                currBean.CopyALLValue(carBean);
                            }

                            EndCarBeanListEntries.remove();
                            //if(backUpCurrentEndCarBeanList.remove(EndCarBeanListEntriesNext.getKey())==null) throw new RuntimeException("数据格式异常");

                            time--;
                            //TODO 是否需要设置前一个时间片正在运行的数据信息
                            trackBackNums = trackBackNums - 1;
                            if (trackBackNums <= 0) break;

                        }
                    }
                    //清空道路上的所有的车辆
                    ClearAllCarInRoad();
                    //车辆上路
                    allStartRunningCarBeanList.sort(new MyCarBackUpComparator());
                    //运行所有车辆
                    for (CarBean carBean : allStartRunningCarBeanList) {
                        carBean.currentRoadBean.carBeanQueues[carBean.channel].add(carBean);
                        //设置车辆的方向
                        //carBean.isSetDirection = carBean.directionCar == 0 && carBean.priorityCar == 0 ? false : carBean.isSetDirection;
                    }
                    if (Constant.IS_ALL_OUT_PRINT == 1) {
                        System.out.println("carBeanList.size(): " + carBeanList.size());
                    }
                    time++;
                    continue;
                }

            }
            //开启所有类型车辆
            driverAllCarInGarage(false, time, null);

            // 已修改 统计空车道的数量 @author wxq
            // 如果关闭全部输出或者不统计空道路数量,此处不运行
            if(Constant.IS_ALL_OUT_PRINT == 1 && Constant.ROAD_SUM_0 == 1){
                zeroCapcityNums = 0;

                for (RoadBean roadBean : roadBeanList){
                    int sum = 0;
                    for (int i = 0; i < roadBean.carBeanQueues.length; i++) {
                        LinkedList<CarBean> carBeanQueue = roadBean.carBeanQueues[i];
                        sum += carBeanQueue.size();

                    }
                    if(sum == 0){
                        zeroCapcityNums++;
                    }
                }
            }
            //已修改结束




            countTheTimeOfAfterDeadLock++;

            if (backUpCurrentNoWaitingCarBeanList.size() < Constant.MEMORY_TIME_COUNT_NUMS) {
                //设置数据
                backUpCurrentEndCarBeanList.put(time, currEndCarBeanList);
                backUpCurrentNoWaitingCarBeanList.put(time, currNoWaitingCarBeanList);
            }else{

                for (Map.Entry<Integer, List<CarBean>> entry : backUpCurrentEndCarBeanList.entrySet()) {
                    if (entry.getKey() != time - Constant.MEMORY_TIME_COUNT_NUMS) {
                        throw new RuntimeException("数据格式异常");
                    }
                    entry.getValue().clear();
                    if (backUpCurrentEndCarBeanList.remove(entry.getKey()) == null)
                        throw new RuntimeException("数据格式异常");
                    backUpCurrentEndCarBeanList.put(time, currEndCarBeanList);
                    break;
                }

                for (Map.Entry<Integer, List<CarBean>> entry : backUpCurrentNoWaitingCarBeanList.entrySet()) {
                    if (entry.getKey() != time - Constant.MEMORY_TIME_COUNT_NUMS) {
                        throw new RuntimeException("数据格式异常");
                    }
                    entry.getValue().clear();
                    if (backUpCurrentNoWaitingCarBeanList.remove(entry.getKey()) == null)
                        throw new RuntimeException("数据格式异常");
                    backUpCurrentNoWaitingCarBeanList.put(time, currNoWaitingCarBeanList);
                    break;
                }
            }


            //更新权重系数  在多次没发生死锁之后,重置道路权重系数
            if (Constant.DEAD_LOCK_CHECK_FLAGE && countTheTimeOfAfterDeadLock > Constant.RESET_WEIGHT_TIME_COUNT)
                for (RoadBean roadBean : roadBeanList) {
                    roadBean.penaltyWeight = 0;
                }
            time++;




        }

        if(Constant.IS_ALL_OUT_PRINT == 1 && Constant.IS_ALLTIME_OUT == 1){
            System.out.println(priorityEndTime - ReaddingTheData.priorityEarlyPlanTime);
            System.out.println(a * (priorityEndTime - ReaddingTheData.priorityEarlyPlanTime) + time);
        }


        BufferedWriter bufferedWriter = null;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(answerPath));
            for (int i = 0; i < carBeanList.size(); i++) {
                CarBean carBean = carBeanList.get(i);
                if (carBean.directionCar == 1) continue;
                StringBuilder builder = new StringBuilder();
                builder.append("(" + carBean.carId + "," + carBean.startTime);
                for (int index = 0; index < carBean.visitedEdges.size(); index++) {
                    builder.append(", " + carBean.visitedEdges.get(index));
                }
                //System.out.println(builder.toString());
                bufferedWriter.write(builder.toString() + ")\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }



    /**
     * 删除道路上所有的车, 回溯的时候用到
     */
    private static void ClearAllCarInRoad() {

        for (RoadBean roadBean : roadBeanList) {
            for (LinkedList<CarBean> carBeans : roadBean.carBeanQueues) {
                carBeans.clear();
            }
        }
    }


   /* private static void drivePreSetCarInGarage(int time) {

        List<CarBean> directionCarBeanList = getDirectionCarBeanList(time);
        for (CarBean carBean : directionCarBeanList) {
            carBean.runTheStartCar(Graph, time);
        }
    }*/

    /**
     * 为所有道路创建一个优先队列,方便后续调用
     */
    private static void createCarSequence() {

        for (RoadBean roadBean : roadBeanList) {
            roadBean.CalThePriorityQueue();
            //System.out.println(roadBean);
        }
    }

    /**
     * 调度等待车辆
     */
    private static boolean driverCarInWaitState(int time) {

        lastWaiting = -1;
        //车辆没有调度完成
        while (!isAllNormalStopStatus()) {

            //按照道路id升序,扫描路口
            for (int i = 0; i < Graph.length; i++) {
                for (ArcBox p = Graph[i].firstin; p != null; p = p.hlink) {
                    LinkedList<CarBean> carBeanPriorityQueues = p.roadBean.carBeanPriorityQueues;
                    while (!carBeanPriorityQueues.isEmpty()) {
                        //调度这辆车
                        CarBean peek = carBeanPriorityQueues.peek();
                        if (!peek.isWaiting) {
                            throw new RuntimeException("数据格式异常");
                        }
                        if (peek.priorityCar == 1) {//优先车
                            if (p.roadBean.endCross != peek.currentRoadBean.endCross) {
                                throw new RuntimeException("程序内部错误");
                            }
                            //到达目的点
                            if (peek.endBean == p.roadBean.endCross) {//peek.endBean == p.roadBean.endCross??

                                if (Constant.DEAD_LOCK_CHECK_FLAGE)
                                    currEndCarBeanList.add(peek.DeepCloneCarBean());
                                //因为是优先车辆直行一定优先,直接结束状态,但是需要更新车辆所在车道后面的数据
                                peek.isFinish = true;
                                peek.isWaiting = false;
                                finishCarNums++;
                                //更新这条车所在车道的所有车辆信息
                                //p.roadBean.carBeanQueues[peek.channel].remove(peek);
                                //直接从道路中移除此辆车
                                p.roadBean.carBeanQueues[peek.channel].poll();
                                //从优先队列中移除
                                //p.roadBean.carBeanPriorityQueues.remove(p.roadBean);
                                ArrangeTheCarForEnd.count--;
                                //更新结束车辆集合
                                if (!allFinishedCarBeanList.add(peek)) throw new RuntimeException("数据异常异常");
                                if (!allStartRunningCarBeanList.remove(peek)) throw new RuntimeException("数据异常异常");
                                //更新后面的车辆信息
                                FlageTheWaitingCar(p.roadBean, peek.channel);
                                //更新优先队列
                                p.roadBean.CalThePriorityQueue();
                                //如果当前车是优先车  设置优先车结束的时间
                                if (peek.priorityCar == 1) priorityEndTime = time;
                                //启动优先车辆  只比较当前道路上的车
                                driverAllCarInGarage(true, time, p.roadBean);
                                continue;
                            } else {//当前没有到达目的地

                                //当前车要走的下一条道路
                                RoadBean nextRoadBean = getRoadBean(p, peek);
                                //存在冲突
                                if (conflict(p, nextRoadBean, peek, i)) break;
                                if (peek.moveToNextRoad(nextRoadBean)) {//没有冲突， 运行
                                    p.roadBean.CalThePriorityQueue();
                                    //启动优先车辆  只比较当前道路上的车 TODO 不仅仅当前道路
                                    driverAllCarInGarage(true, time, p.roadBean);
                                    continue;
                                } else {//移动失败
                                    break;
                                }
                            }
                        } else {//当前车辆普通车辆


                            if(peek.currentRoadBean.roadId!=p.roadBean.roadId) throw new RuntimeException("程序内部逻辑错误");


                            if (peek.endBean == p.roadBean.endCross) {//普通车辆直行,  只存在优先级车辆的干扰
                                //TODO  找到当前车直行方向对应的位置
                                //可能不存在
                                //p.roadBean.startCross--->p.roadBean.endCross--->? 直行
                                //新的方向  相对于p.roadBean.endCross
                                RoadBean roadBean= FindTheNextRoadIdByDirection(i, p);
                                // 如果哪条道路存在且阻挡
                                if (roadBean != null && BlockingFlagesNoProiorityCar(p, i, null, roadBean.roadId)) {
                                    break;
                                }

                                if (Constant.DEAD_LOCK_CHECK_FLAGE)
                                    currEndCarBeanList.add(peek.DeepCloneCarBean());
                                peek.isWaiting = false;
                                peek.isFinish = true;
                                finishCarNums++;
                                peek.currentRoadBean.carBeanQueues[peek.channel].poll();
                                ArrangeTheCarForEnd.count--;
                                FlageTheWaitingCar(p.roadBean, peek.channel);
                                if (!allFinishedCarBeanList.add(peek))throw new RuntimeException("添加数据失败");
                                if (!allStartRunningCarBeanList.remove(peek)) throw new RuntimeException("删除失败");
                                //更新优先队列
                                p.roadBean.CalThePriorityQueue();
                                if (peek.priorityCar == 1) throw new RuntimeException("优先级数据异常");
                                //启动优先车辆  只比较当前道路上的车
                                driverAllCarInGarage(true, time, p.roadBean);
                            } else {
                                RoadBean nextRoadBean = getRoadBean(p, peek);
                                //存在冲突
                                if (conflict(p, nextRoadBean, peek, i)) break;
                                if (peek.moveToNextRoad(nextRoadBean)) {//没有冲突， 运行
                                    p.roadBean.CalThePriorityQueue();
                                    //启动优先车辆  只比较当前道路上的车 TODO 不仅仅当前道路
                                    driverAllCarInGarage(true, time, p.roadBean);
                                    continue;
                                } else {//移动失败 第一优先级产生冲突, 直接开始新的道路遍历
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            if (deadLock()) return false;
        }
        return true;
    }

    /**
     * p.roadBean.startCross--->p.roadBean.endCross--->? 直行
     *
     * @param i
     * @param p TODO  direction 方向参数后期添加
     * @return 指定方向的下一跳道路id, 如果不存在返回-1
     */
    private static RoadBean FindTheNextRoadIdByDirection(int i, ArcBox p) {
        int newDirection = p.headDirection - 2 < 0 ? (p.headDirection - 2 + 4) % 4 : (p.headDirection - 2) % 4;
        //其实就是车将要走的哪条道路
        RoadBean roadBean =null;
        for (ArcBox m = Graph[i].firstout; m != null; m = m.tlink) {
            if (m.tailDirection == newDirection) {
                roadBean = m.roadBean;
                break;
            }
        }
        return roadBean;
    }

    private static boolean deadLock() {

        int countWaitingCarNum = 0;
        for (CarBean carBean : allStartRunningCarBeanList) {
            if (carBean.isWaiting) {
                countWaitingCarNum++;
            }
        }

        boolean isdeadLock = false;

        //System.out.println("lastWaiting: " + lastWaiting + " countWaitingCarNum: " + countWaitingCarNum);

        if (lastWaiting == countWaitingCarNum) {
            isdeadLock = true;
        } else {
            lastWaiting = countWaitingCarNum;
            isdeadLock = false;
        }

        return isdeadLock;
    }

    /**
     * 找到当前车辆走的下一个道路
     *
     * @param p    这条车辆所在的弧
     * @param peek 车辆
     * @return 当前车辆走的下一个道路
     */
    private static RoadBean getRoadBean(ArcBox p, CarBean peek) {

        //优先的预置车辆 交叉入口调度
        int roadId = -1;
        //预置车辆
        if (peek.directionCar == 1) {
            roadId = peek.visitedEdges.get(peek.index);

        } else if (peek.isSetDirection) {//非预置车辆,设置过方向
            roadId = peek.visitedEdges.get(peek.visitedEdges.size() - 1);
        } else {//没设置过方向
            //设置方向   TODO  问题重复计算nextRoadBean
            RoadBean roadBean = GainTheCarDirection(peek);
            roadId = roadBean.roadId;
        }
        if (roadId == -1) throw new RuntimeException("数据格式异常");

        //int roadId = peek.visitedEdges.get(peek.index);
        RoadBean roadBean = p.roadBean;
        RoadBean nextRoadBean = null;
        for (ArcBox q = Graph[LocateVex(roadBean.endCross)].firstout; q != null; q = q.tlink) {
            if (q.roadBean.roadId == roadId) {
                nextRoadBean = q.roadBean;
                break;
            }
        }
        if (nextRoadBean == null) throw new RuntimeException("数据格式异常");
        return nextRoadBean;
    }


    /**
     * 判断是否产生冲突
     *
     * @param p            弧
     * @param nextRoadBean 当前车要进入的下一道路
     * @param peek         当前车
     * @param i            当前车所在道路的尾交叉出口索引
     * @return true 产生冲突
     */
    public static boolean conflict(ArcBox p, RoadBean nextRoadBean, CarBean peek, int i) {

        int direction = FindTheDirection(LocateVex(p.roadBean.startCross), LocateVex(p.roadBean.endCross), LocateVex(nextRoadBean.endCross));
        if (peek.priorityCar == 1) {//优先车辆

            if (direction == Constant.STRAIGHT) {//直行不能存在冲突
                return false;
            } else if (direction == Constant.LEFT) {//直行冲突
                //直行道路
                RoadBean strRoadBean = null;
                int newDirection = p.headDirection - 1 < 0 ? (4 + p.headDirection - 1) % 4 : (p.headDirection - 1) % 4;
                for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                    if (q.headDirection == newDirection) { //找出直行对应的道路
                        strRoadBean = q.roadBean;
                        break;
                    }
                }
                //对于优先车辆只可能存在优先级车的直行冲突
                if (strRoadBean != null && BlockingFlagesPriorityCar(strRoadBean, Constant.STRAIGHT, nextRoadBean.roadId)) {
                    return true;
                }
            } else if (direction == Constant.RIGHT) {// 右转 存在直行和左转冲突

                RoadBean strRoadBean = null;
                RoadBean leftRoadBean = null;
                int strDirection = (p.headDirection + 1) % 4;
                int leftDirection = p.headDirection - 2 < 0 ? (4 + p.headDirection - 2) % 4 : (p.headDirection - 2) % 4;
                for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                    if (strRoadBean != null && leftRoadBean != null) break;
                    if (q.headDirection == strDirection) { //找出直行对应的道路
                        strRoadBean = q.roadBean;
                    }
                    if (q.headDirection == leftDirection) {
                        leftRoadBean = q.roadBean;
                    }
                }
                //找出直行对应的道路
                if (strRoadBean != null && BlockingFlagesPriorityCar(strRoadBean, Constant.STRAIGHT, nextRoadBean.roadId)) {
                    //index++;
                    //continue;
                    return true;
                }
                //找出左转对应的道路
                if (leftRoadBean != null && BlockingFlagesPriorityCar(leftRoadBean, Constant.LEFT, nextRoadBean.roadId)) {
                    //index++;
                    //continue;
                    return true;
                }
            }
        } else {//普通车辆

            if (direction == Constant.STRAIGHT) {//直行可能存在冲突 , null 表示需要判断的条件为空 , 只有优先级车辆才能打断当前直行
                return BlockingFlagesNoProiorityCar(p, i, null, nextRoadBean.roadId);

            } else if (direction == Constant.LEFT) {
                //找出普通车辆存在冲突的位置
                //直行道路
                RoadBean strRoadBean = null;
                int newDirection = p.headDirection - 1 < 0 ? (4 + p.headDirection - 1) % 4 : (p.headDirection - 1) % 4;
                for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                    if (q.headDirection == newDirection) { //找出直行对应的道路
                        strRoadBean = q.roadBean;
                        break;
                    }
                }
                List<RoadBean> arcboxs=new ArrayList<>();


                if(strRoadBean!=null) arcboxs.add(strRoadBean);

                if(arcboxs.size()==0) arcboxs = null;

                return BlockingFlagesNoProiorityCar(p, i, arcboxs, nextRoadBean.roadId);


            } else if (direction == Constant.RIGHT) {


                RoadBean strRoadBean = null;
                RoadBean leftRoadBean = null;
                int strDirection = (p.headDirection + 1) % 4;
                int leftDirection = p.headDirection - 2 < 0 ? (4 + p.headDirection - 2) % 4 : (p.headDirection - 2) % 4;
                for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                    if (strRoadBean != null && leftRoadBean != null) break;
                    if (q.headDirection == strDirection) { //找出直行对应的道路
                        strRoadBean = q.roadBean;
                    }
                    if (q.headDirection == leftDirection) {
                        leftRoadBean = q.roadBean;
                    }
                }
                List<RoadBean> arcboxs = new ArrayList<>();
                if (leftRoadBean != null) arcboxs.add(leftRoadBean);
                if (strRoadBean != null) arcboxs.add(strRoadBean);
                if (arcboxs.size() == 0) arcboxs = null;
                return BlockingFlagesNoProiorityCar(p, i, arcboxs, nextRoadBean.roadId);


            } else {
                throw new RuntimeException("方向计算异常");
            }
        }
        return false;
    }

    /**
     * 对于非优先级车辆判断是否阻挡
     *
     * @param p  车辆所在的弧
     * @param i
     * @param arcboxs 一些非优先级的车所在道路能够阻挡。（本质上是条件）
     * @return
     */
    private static boolean BlockingFlagesNoProiorityCar(ArcBox p, int i, List<RoadBean> arcboxs, int nextRoadId) {

        boolean isOk = false;

        //先判断所有的优先车辆是否会干扰当前数据
        for (ArcBox k = Graph[i].firstin; k != null; k = k.hlink) {

            //如果相应的优先队列为空或者 和当前车辆在同一个弧上
            if (k.equals(p)) continue;
            //k.roadBean.RemoveTheStopCarFromPriorityQueue();
            if (k.roadBean.carBeanPriorityQueues.isEmpty()) continue;
            CarBean peek1 = k.roadBean.carBeanPriorityQueues.peek();
            isOk = false;
            if (arcboxs != null) {
                //主要是为了添加那些非优先级车的阻挡限制条件
                for (RoadBean arcBox : arcboxs) {
                    if (arcBox.equals(k.roadBean)) isOk = true;
                }
            }

            //当前车是优先级车  或者是满足条件的非优先车所在的道路
            if (peek1.priorityCar == 1 || isOk) {

                if (!peek1.isWaiting) throw new RuntimeException("内部逻辑错误");

                //当前节点到达目的地
                if (peek1.endBean == peek1.currentRoadBean.endCross) {
                    //TODO 找到方向数据
                    //peek1.startBean-->peek1.endBean-->? 直行
                    RoadBean roadBean= FindTheNextRoadIdByDirection(i, k);
                    //如果相等说明存在干扰  roadId=-1是肯定不存在干扰(不可能相等不需要额外判断)
                    if (roadBean!=null&&roadBean.roadId == nextRoadId) return true;
                    else continue;
                }
                int roadId = -1;
                //当前车是预置车
                if (peek1.directionCar == 1) {
                    roadId = peek1.visitedEdges.get(peek1.index);
                } else if (peek1.isSetDirection) {
                    roadId = peek1.visitedEdges.get(peek1.visitedEdges.size() - 1);
                } else {
                    roadId = GainTheCarDirection(peek1).roadId;
                }

                if (roadId == -1) throw new RuntimeException("数据格式异常");

                //当前车不会干扰当前车  直接判断下一辆车
                //if (roadId != nextRoadId) continue;
                if (roadId == nextRoadId) return true;
                //else continue;
            }
        }
        //没有阻挡
        return false;
    }


    /**
     * 等待转弯的车辆是优先车辆
     *
     * @param strRoadBean 表示当前判断的道路
     * @param direction   当前道路是否存在direction方向上的等待转弯车辆
     * @return false 表示不存在堵住的情况    true表示存在堵住
     */

    private static boolean BlockingFlagesPriorityCar(RoadBean strRoadBean, int direction, int nextRoadId) {

        //移除优先队列中失效的元素
        // strRoadBean.RemoveTheStopCarFromPriorityQueue();
        LinkedList<CarBean> carBeanPriorityQueues = strRoadBean.carBeanPriorityQueues;
        //当前优先队列为空
        if (carBeanPriorityQueues.isEmpty()) {
            return false;
        } else {//当前队列不为空
            CarBean peek = carBeanPriorityQueues.peek();

            if (!peek.isWaiting) throw new RuntimeException("数据格式异常");
            //非优先级车
            if (peek.priorityCar == 0) return false;
            else {//当前车也是优先级车辆
                int roadId = -1;
                //peek车到达目的地,相当于直行,可能存在冲突
                if (peek.endBean == strRoadBean.endCross) {

                    return direction == Constant.STRAIGHT;
                }
                if (peek.directionCar == 1) {//预置车辆
                    roadId = peek.visitedEdges.get(peek.index);
                } else if (peek.isSetDirection) {//设置过方向
                    roadId = peek.visitedEdges.get(peek.visitedEdges.size() - 1);
                } else {//没设置过方向
                    //设置方向, 重复计算问题
                    roadId = GainTheCarDirection(peek).roadId;
                }
                if (roadId == -1) throw new RuntimeException("数据格式异常");
                /* int endCrossIndex = LocateVex(strRoadBean.endCross);
                int nendCrossIndex = -1;
                for (ArcBox k = Graph[endCrossIndex].firstout; k != null; k = k.tlink) {

                    if (k.roadBean.roadId == roadId) {
                        nendCrossIndex = k.roadBean.endCross;
                        break;
                    }
                }
                if (nendCrossIndex == -1) throw new RuntimeException("数据格式异常");
                nendCrossIndex = LocateVex(nendCrossIndex);
                return isInTheSameDirection(LocateVex(strRoadBean.startCross), endCrossIndex, nendCrossIndex, direction);*/

                return roadId == nextRoadId;
            }
        }
    }

    /**
     * 获取当前车下一个方向
     *
     * @param peek 当前车
     * @return
     */
    public static RoadBean GainTheCarDirection(CarBean peek) {


        if (peek.isSetDirection) throw new RuntimeException("逻辑异常");
        // 已修改 , 添加车辆参数 @author wxq
        CheckThePreparingStartingRoad(peek);
        //先计算相对最短路径 ，再计算绝对最短路径
        int[][] path = new int[Graph.length][Graph.length];
        int[] visited = new int[Graph.length];
        //到达调度时间
        ArrangeTheCarForEnd.ShortestPath_DJSTR(LocateVex(peek.currentRoadBean.startCross), LocateVex(peek.currentRoadBean.endCross), path, visited, LocateVex(peek.endBean));
        RoadBean roadBean = null;
        int nextCrossIndex = -1;
        if (visited[LocateVex(peek.endBean)] == 0) {
            //throw new RuntimeException("计算异常");
            path = new int[Graph.length][Graph.length];
            ShortestPath_DJSTRAbSolute(LocateVex(peek.currentRoadBean.startCross), LocateVex(peek.currentRoadBean.endCross), path, visited, LocateVex(peek.endBean));
            if (visited[ArrangeTheCarForEnd.LocateVex(peek.endBean)] == 0) throw new RuntimeException("数据格式异常");
            nextCrossIndex = path[LocateVex(peek.endBean)][1];
        } else {
            nextCrossIndex = path[LocateVex(peek.endBean)][1];
        }
        for (ArcBox p = Graph[ArrangeTheCarForEnd.LocateVex(peek.currentRoadBean.endCross)].firstout; p != null; p = p.tlink) {
            if (LocateVex(p.roadBean.endCross) == nextCrossIndex) {
                roadBean = p.roadBean;
                break;
            }
        }
        if (roadBean == null) {
            System.out.println(peek.currentRoadBean.startCross + "  " + peek.currentRoadBean.endCross + " " + nextCrossIndex);
            throw new RuntimeException("数据格式异常");
        }

        //标注已经设置过方向
        peek.isSetDirection = true;
        peek.visitedEdges.add(roadBean.roadId);
        return roadBean;
    }

    //一个时间片运行完成
    private static boolean isAllNormalStopStatus() {

        /*for (int i = 0; i < allStartRunningCarBeanList.size(); i++) {
            if (carBeanList.get(i).isWaiting) {
                return false;
            }
        }*/
        for (CarBean carBean : allStartRunningCarBeanList) {
            if (carBean.isWaiting) return false;
        }
        return true;
    }

    /**
     * 判断所有的车辆是否到达目的地
     *
     * @return true 所有车到达目的地, false 有车没到达
     */
    private static boolean isFinish() {
        for (CarBean carBean : carBeanList) {
            if (!carBean.isFinish) return false;
        }
        return true;
    }

    /**
     * 构建十字交叉链表
     *
     * @return
     */
    private static VexNode[] BuildTheVertecNodesMap() {
        VexNode[] vertexNodes = new VexNode[crossBeanList.size()];

        //先初始化数据
        for (int i = 0; i < vertexNodes.length; i++) {
            CrossBean crossBean = crossBeanList.get(i);
            VexNode vertexNode = new VexNode(crossBean.crossId);
            vertexNodes[i] = vertexNode;
        }

        Graph = vertexNodes;

        //图构建
        //生成边数
        for (int e = roadBeanList.size() - 1; e >= 0; e--) {
            RoadBean roadBean = roadBeanList.get(e);
            int startCross = roadBean.startCross;
            int endCross = roadBean.endCross;
            int i = LocateVex(startCross);
            int j = LocateVex(endCross);
            int tailDirection = -1;
            int headDirection = -1;
            for (CrossBean crossBean : crossBeanList) {//遍历每一个交叉口
                //数据方向已经找到
                if (tailDirection != -1 && headDirection != -1) break;

                if (tailDirection == -1 && crossBean.crossId == startCross) {

                    if (tailDirection == -1 && crossBean.upRoadId != -1 && crossBean.upRoadId == roadBean.roadId) {
                        tailDirection = Constant.UPD;
                    }

                    if (tailDirection == -1 && crossBean.rightRoadId != -1 && crossBean.rightRoadId == roadBean.roadId) {
                        tailDirection = Constant.RIGHTD;
                    }

                    if (tailDirection == -1 && crossBean.downRoadId != -1 && crossBean.downRoadId == roadBean.roadId) {
                        tailDirection = Constant.DOWND;
                    }

                    if (tailDirection == -1 && crossBean.leftRoadId != -1 && crossBean.leftRoadId == roadBean.roadId) {
                        tailDirection = Constant.LEFTD;
                    }
                }

                if (headDirection == -1 && crossBean.crossId == endCross) {

                    if (crossBean.upRoadId != -1 && crossBean.upRoadId == roadBean.roadId) {
                        headDirection = Constant.UPD;
                    }

                    if (headDirection == -1 && crossBean.rightRoadId != -1 && crossBean.rightRoadId == roadBean.roadId) {
                        headDirection = Constant.RIGHTD;
                    }

                    if (headDirection == -1 && crossBean.downRoadId != -1 && crossBean.downRoadId == roadBean.roadId) {
                        headDirection = Constant.DOWND;
                    }

                    if (headDirection == -1 && crossBean.leftRoadId != -1 && crossBean.leftRoadId == roadBean.roadId) {
                        headDirection = Constant.LEFTD;
                    }
                }
            }
            ArcBox arcBox = new ArcBox(i, j, vertexNodes[j].firstin, vertexNodes[i].firstout, roadBean, tailDirection, headDirection);
            //设置图中链头的插入
            vertexNodes[i].firstout = vertexNodes[j].firstin = arcBox;
        }
        return vertexNodes;
    }


    /**
     * 获取交叉路口id对应的数组下表索引
     *
     * @param crossId
     * @return
     */
    public static int LocateVex(int crossId) {

        for (int i = 0; i < Graph.length; i++) {

            if (Graph[i].crossId == crossId) {
                return i;
            }
        }

        throw new RuntimeException("内部数据错误");
    }


    /**
     * 便利每一条道路
     * 标记等待车辆和终止车辆,
     *
     * @param curroadBean 如果此值为null 表示标定所有道路, 否则标定指定道路
     *                    TODO 不能在这一步设置结束状态,需要等待调度才进行结束判断
     */
    public static void FlageTheWaitingCar(RoadBean curroadBean, int channel) {
        //遍历每一条道路
        if (curroadBean == null) {
            for (RoadBean roadBean : roadBeanList) {
                driveJustCurrentRoad(roadBean);
            }
        } else {
            driveJustCurrentRoad(curroadBean, channel);
        }
    }


    private static void driveJustCurrentRoad(RoadBean roadBean, int channel) {



        LinkedList<CarBean> carBeanQueue = roadBean.carBeanQueues[channel];

        //每条车道的最早进入元素
        for (int index = 0; index < carBeanQueue.size(); index++) {
            CarBean carBean = carBeanQueue.get(index);
            //当前车辆为终止状态
            if (!carBean.isWaiting) break;
            //carBean 后一辆车
            CarBean preBean = index == 0 ? null : carBeanQueue.get(index - 1);
            //当前车距离上一辆车的距离    如果当前车没有上一辆车  直接是到尽头的距离
            int distance = index == 0 ? carBean.s1 : carBean.s1 - preBean.s1 - 1;
            if(distance<=0) throw new RuntimeException("长度数据异常");
            //之前车辆的速度   如果之前车辆是等待状态 那么当前车以min(道路最大限速,车辆最大速度)   否则 当前车min(道路最大限速,车辆最大速度,两辆车距离之差)
            int preCarMaxSpeed = index == 0 ? roadBean.speedLimit : preBean.isWaiting ? roadBean.speedLimit : distance;
            //求出当前车的速度
            carBean.currSpeed = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), preCarMaxSpeed);
            if (carBean.currSpeed > distance) {//如果当前车速大于距离
                //carBean.isWaiting = true;
                break;
            } else {//不超过距离
                if (Constant.DEAD_LOCK_CHECK_FLAGE) {

                    currNoWaitingCarBeanList.add(carBean.DeepCloneCarBean());
                }
                carBean.isWaiting = false;
                //等待车辆减少
                //roadBean.waitingTheCarNums--;
                carBean.s1 -= carBean.currSpeed;
            }


            //当前车的速度行驶大于distance可能存在出去
            /*if (carBean.currSpeed >=distance) {
                if (index == 0) {//当前车道最前车
                    if (carBean.endBean == roadBean.endCross) {
                        //到达目的地也需要等待状态,为了后面调度,相当于直行
                        carBean.isWaiting = true;
                        break;

                    } else {//没有到达目的地
                        //标注终止状态
                        if (carBean.currSpeed == distance) {//正好到达当前道路的最前面
                            carBean.isWaiting = false;
                            carBean.s1 = 0;
                        } else {//可能会转弯
                            carBean.isWaiting = true;
                            break;
                        }
                        continue;
                    }
                } else {//不是最前车

                    //前面那辆车是等待状态
                    if (preBean.isWaiting) {
                        //TODO 修改 ==变为<=  由于速度小于两者之间的距离, 没必要修改由于前面已经判断不可能小于
                        if (carBean.currSpeed == distance) {
                            carBean.isWaiting = false;
                            carBean.s1 -= carBean.currSpeed;
                        } else {//等待状态
                            carBean.isWaiting = true;
                            break;
                        }
                    } else {//前面那辆车是终止状态
                        if (carBean.currSpeed > distance) throw new RuntimeException("逻辑错误");
                        carBean.isWaiting = false;
                        carBean.s1 -= carBean.currSpeed;
                    }
                }

            } else {//不能转弯,直接终止
                carBean.isWaiting = false;
                carBean.s1 -= carBean.currSpeed;
            }*/

        }


    }

    private static void driveJustCurrentRoad(RoadBean roadBean) {
        LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
        // if (roadBean.waitingTheCarNums != 0) throw new RuntimeException("waitingTheCarNums设置有问题");
        //遍历每一个车道
        for (LinkedList<CarBean> carBeanLinkedList : carBeanQueues) {
            //每条车道的最早进入元素
            for (int index = 0; index < carBeanLinkedList.size(); index++) {
                CarBean carBean = carBeanLinkedList.get(index);

                //carBean 后一辆车
                CarBean preBean = index == 0 ? null : carBeanLinkedList.get(index - 1);

                //当前车距离上一辆车的距离    如果当前车没有上一辆车  直接是到尽头的距离
                int distance = index == 0 ? carBean.s1 : carBean.s1 - preBean.s1 - 1;

                //之前车辆的速度   如果之前车辆是等待状态 那么当前车以min(道路最大限速,车辆最大速度)   否则 当前车min(道路最大限速,车辆最大速度,两辆车距离之差)
                int preCarMaxSpeed = index == 0 ? roadBean.speedLimit : preBean.isWaiting ? roadBean.speedLimit : distance;

                //求出当前车的速度
                carBean.currSpeed = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), preCarMaxSpeed);
                if (carBean.currSpeed > distance) {//如果当前车速大于距离,
                    //等待车辆个数加1
                    // roadBean.waitingTheCarNums++;
                    carBean.isWaiting = true;
                } else {//不超过距离

                    if (Constant.DEAD_LOCK_CHECK_FLAGE)
                        currNoWaitingCarBeanList.add(carBean.DeepCloneCarBean());
                    carBean.isWaiting = false;
                    carBean.s1 -= carBean.currSpeed;
                }
                //当前车的速度行驶大于distance可能存在出去
                  /*if (carBean.currSpeed >= distance) {
                    if (index == 0) {//当前车道最前车
                        if (carBean.endBean == roadBean.endCross) {
                            //到达目的地也需要等待状态,为了后面调度,相当于直行
                            carBean.isWaiting = true;
                            continue;

                        } else {//没有到达目的地
                            //标注终止状态
                            if (carBean.currSpeed == distance) {//正好到达当前道路的最前面
                                carBean.isWaiting = false;
                                carBean.s1 = 0;
                                continue;
                            } else {//可能会转弯
                                carBean.isWaiting = true;
                                continue;
                            }
                        }
                    } else {//不是最前车

                        //前面那辆车是等待状态
                        if (preBean.isWaiting) {
                            //TODO 修改 ==变为<=  由于速度小于两者之间的距离, 没必要修改由于前面已经判断不可能小于
                            if (carBean.currSpeed <= distance) {
                                carBean.isWaiting = false;
                                carBean.s1 -= carBean.currSpeed;
                            } else {//等待状态
                                carBean.isWaiting = true;
                            }
                        } else {//前面那辆车是终止状态
                            if (carBean.currSpeed > distance) throw new RuntimeException("逻辑错误");
                            carBean.isWaiting = false;
                            carBean.s1 -= carBean.currSpeed;
                        }
                    }

                } else {//不能转弯,直接终止
                    carBean.isWaiting = false;
                    carBean.s1 -= carBean.currSpeed;
                }*/

            }
        }
    }


    /**
     * 开启车库中的车辆
     *
     * @param priority true表示只开启优先级车辆, false 都开启
     * @param time     当前时间片
     */
    public static void driverAllCarInGarage(boolean priority, int time, RoadBean roadBean) {
        /**
         * 两种情况 先调用预置车辆,再调用其它种类的车辆
         */
        //所有非预置车辆
        List<CarBean> carBeanList = priority ? (roadBean == null ? getPriorityCarBeanList(time) : getPriorityCarBeanList(time, roadBean)) : getAllUnStartCarBeanList(time);
        //针对每一个未开启的车辆
        for (CarBean carBean : carBeanList) {
            //超过给定阈值,车辆不允许开启 主要针对普通车辆。优先级车辆无效 Constant.MAX_CAR_NUMS+ ArrangeTheCarForEnd.time*2.9

            //已修改 @author wxq
            //增加功能: 如果后续没有优先和预置启动,按照参数增加地图车辆
            int addCarNums = 0;
            if (futureStartNums[time < planLength ? time : planLength-1] == 0){
                addCarNums = Constant.ADD_ALLCARNUMS;
            }
            int remainCars = 0;
            if (allUnStartCarBeanList.size() <= Constant.remainCars){
                remainCars =  Constant.remainCars;
            }

            if (carBean.priorityCar == 0 && carBean.directionCar == 0 &&
                    (allStartRunningCarBeanList.size() + futureStartNums[time < planLength ? time :
                            planLength-1] - Constant.FUTURE_END_NUMS) >= (Constant.MAX_CAR_NUMS  + addCarNums + remainCars)) continue;
            carBean.runTheStartCar(Graph, time);
        }
    }

    /**
     * 获取所有未启动的优先级车辆
     *
     * @param time 当前时刻没有启动的车辆
     * @return
     */
    public static List<CarBean> getPriorityCarBeanList(int time) {
        List<CarBean> priorityCarBeanList = new ArrayList<>();
        for (CarBean carBean : allUnStartCarBeanList) {//当前时间允许启动的车辆
            if (carBean.isStart) throw new RuntimeException("内部数据格式异常");
            if (carBean.priorityCar == 1 && time >= carBean.startTime) {
                //非预置车辆时间属性更新
                carBean.startTime = (carBean.directionCar != 1 && time > carBean.startTime) ? time : carBean.startTime;
                //carBean.startTime = time;
                priorityCarBeanList.add(carBean);
            }
        }
        Collections.sort(priorityCarBeanList);
        return priorityCarBeanList;
    }


    /**
     * 循环启动优先车辆的时候调用
     * @param time
     * @param roadBean
     * @return
     */
    public static List<CarBean> getPriorityCarBeanList(int time, RoadBean roadBean) {

        List<CarBean> priorityCarBeanList = new ArrayList<>();
        for (CarBean carBean : allUnStartCarBeanList) {//当前时间允许启动的车辆
            if (carBean.isStart) throw new RuntimeException("内部数据格式异常");
            if (carBean.priorityCar == 1 && time >= carBean.startTime) {
                if (!carBean.isSetDirection&&carBean.directionCar==0) {
                    throw new RuntimeException("没设置过方向");
                }
                if (carBean.startBean == roadBean.startCross && carBean.visitedEdges.get(0) == roadBean.roadId) {
                    carBean.startTime = (carBean.directionCar != 1 && time > carBean.startTime) ? time : carBean.startTime;
                    priorityCarBeanList.add(carBean);
                }
            }
        }
        Collections.sort(priorityCarBeanList);
        return priorityCarBeanList;
    }


    /**
     * 获取所有未启动的车辆  包括优先车辆和非优先车辆
     *
     * @return
     */
    public static List<CarBean> getAllUnStartCarBeanList(int time) {
        List<CarBean> unStartCarBeanList = new ArrayList<>();
        for (CarBean carBean : allUnStartCarBeanList) {
            if (carBean.isStart) throw new RuntimeException("内部数据格式异常");
            if (time >= carBean.startTime) {
                //保证时间有效性  不为-1
                carBean.startTime = (carBean.directionCar != 1 && time > carBean.startTime) ? time : carBean.startTime;
                //carBean.startTime = time;
                unStartCarBeanList.add(carBean);
            }
        }
        Collections.sort(unStartCarBeanList);

        return unStartCarBeanList;
    }


    /**
     * 准备开始运行  那些还没开始运动的点
     */
    // 已修改 @author wxq
    public static void CheckThePreparingStartingRoad(CarBean carBean) {
        //遍历其他边
        for (int i = 0; i < roadBeanList.size(); i++) {
            boolean isSet = false;
            RoadBean roadBean = roadBeanList.get(i);
            LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
            for (int k = 0; k < carBeanQueues.length; k++) {
                LinkedList<CarBean> carBeanQueue = carBeanQueues[k];
                if (carBeanQueue.size() == 0) {
                    roadBean.visible = true;
                    isSet = true;
                    break;
                } else {
                    CarBean carBean1 = carBeanQueue.peekLast();
                    if (carBean1.isWaiting) {
                        roadBean.visible = true;
                        isSet = true;
                        break;
                    } else {
                        if (carBean1.s1 == roadBean.roadLength - 1) {
                            continue;
                        }
                        isSet = true;
                        roadBean.visible = true;
                        break;
                    }
                }
            }
            if (!isSet) roadBean.visible = false;

        }
        //扫描一遍所有的道路  求出其中相对长度
        //checkTheCrossBusiness();
        checkTheRoadRelativeBusiness(carBean);
    }

    /**
     * 设置每个交叉入口的拥堵情况
     */
    private static void checkTheCrossBusiness() {
        for (int i = 0; i < Graph.length; i++) {
            int AllCarSize = 0;
            int CarRunningInTheCar = 0;
            for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {

                int[] nums = FindTheWaitingCarInLane(q.roadBean);
                if (nums.length != 2) throw new RuntimeException("长度数据异常");
                AllCarSize += nums[0];
                CarRunningInTheCar += nums[1];
            }
            Graph[i].crossBusiness = AllCarSize == 0 ? 0 : CarRunningInTheCar * 1.0 / AllCarSize;
        }
    }


    /**
     * 设置道路的相对长度
     */
    public static void checkTheRoadRelativeBusiness(CarBean carBean) {

        for (RoadBean roadBean : roadBeanList) {
            //道路上的总的车辆数
            int sum = 0;
            //道路尾部空间
            int spaceFreeCount = 0;
            //平均速度
            int averageSpeed = 0;
            //保存每条车道车的最小速度, 可以不用
            int[] speedArray = new int[roadBean.roadNums];
            for (int i = 0; i < roadBean.carBeanQueues.length; i++) {
                LinkedList<CarBean> carBeanQueue = roadBean.carBeanQueues[i];
                sum += carBeanQueue.size();
                //CarBean carBean1 = carBeanQueue.peekLast();
                //spaceFreeCount += (carBean1 == null ? carBeanQueue.size() : carBeanQueue.size() - carBean1.s1 - 1);
                /*int currSpeed = roadBean.speedLimit;
                for (CarBean cr : carBeanQueue) {
                    currSpeed  = Math.min(currSpeed , cr.maxSpeed);
                }
                speedArray[i] = currSpeed;
                averageSpeed += speedArray[i];*/
            }//0.2效果最优
            //averageSpeed = averageSpeed * 1.0 / roadBean.roadNums;

            //道路的相对拥堵情况   Math.min(roadBean.speedLimit,carBean.maxSpeed)
            //roadBean.relativeRoadBlock = Constant.W0 *  roadBean.roadLength - Constant.W1 * roadBean.speedLimit + sum*1.0*Constant.W2 / roadBean.roadNums - Constant.W4 * spaceFreeCount;

            //已修改 @author wxq
            //添加新的道路权重方程
            if(carBean == null){
                throw new RuntimeException("车辆为空");
               // roadBean.relativeRoadBlock = Constant.W0 * roadBean.roadLength - Constant.W1 * roadBean.speedLimit + sum * 1.0 * Constant.W2 / roadBean.roadNums;
            }

            double w5Value = Double.MIN_VALUE;
            if(roadBean.speedLimit <= carBean.maxSpeed){
                roadBean.relativeRoadBlock = Constant.W0 * roadBean.roadLength - Constant.W1 * roadBean.speedLimit + sum * 1.0 * Constant.W2 / roadBean.roadNums +roadBean.penaltyWeight;
            }else {
                w5Value = Math.exp(Math.abs(((double) roadBean.speedLimit / carBean.maxSpeed) -1));
                //python road1.speed < car1.speed -> weight = (road1.length / car1.speed) + (road1.length / road1.channel) + Math.pow(Math.E, Math.abs((road1.speed / car1.speed) - 1));
                roadBean.relativeRoadBlock = Constant.W0 * roadBean.roadLength - Constant.W1 * roadBean.speedLimit + sum * 1.0 * Constant.W2 / roadBean.roadNums + Constant.w5 * w5Value + roadBean.penaltyWeight;

            }

            // 统计道路信息
            if (Constant.IS_ALL_OUT_PRINT == 1 && Constant.IS_PRINT_WEIGHTS == 1){
                if (time == tempTime){
                    roadlengths.add(roadBean.roadLength);
                    speedLimits.add(roadBean.speedLimit);
                    capacitys.add(((double)sum / roadBean.roadNums));
                    if(w5Value != Double.MIN_VALUE){
                        w5Values.add(w5Value);
                    }
                }else {
                    // 如果时间不一致,即当前时间片已结束, 进行统计.
                    int maxRoadLength = 0, minRoadLength = Integer.MAX_VALUE, sumRoadLength = 0;
                    int maxSpeedLimit = 0, minSpeedLimit = Integer.MAX_VALUE, sumSpeedLimit = 0;
                    double maxCapacity = 0, minCapacity = Double.MAX_VALUE, sumCapacity = 0;
                    double maxW5Value = 0l, minW5Value = Double.MAX_VALUE, sumW5Value = 0 ;

                    for (Integer roadlength : roadlengths) {
                        if(roadlength > maxRoadLength){
                            maxRoadLength = roadlength;
                        }

                        if (roadlength < minRoadLength){
                            minRoadLength = roadlength;

                        }

                        sumRoadLength += roadlength;
                    }

                    for (Integer speedLimit : speedLimits) {
                        if (speedLimit > maxSpeedLimit){
                            maxSpeedLimit = speedLimit;
                        }

                        if (speedLimit < minSpeedLimit){
                            minSpeedLimit = speedLimit;
                        }

                        sumSpeedLimit += speedLimit;
                    }

                    for (Double capacity : capacitys) {
                        if (capacity > maxCapacity){
                            maxCapacity = capacity;
                        }

                        if (capacity < minCapacity){
                            minCapacity = capacity;

                        }

                        sumCapacity += capacity;
                    }

                    for (Double value : w5Values) {
                        if (value > maxW5Value){
                            maxW5Value = value;
                        }

                        if (value < minW5Value){
                            minW5Value = value;
                        }

                        sumW5Value += value;
                    }

                    System.out.print("统计权重: " + tempTime);
                    System.out.println(" 最大拥堵: " + maxCapacity + " 最小拥堵: " + minCapacity + " 空车道数量: " + zeroCapcityNums + " 平均拥堵: " + sumCapacity/capacitys.size()
                                     + " 最大比重: " + maxW5Value + " 最小比重: " + minW5Value + " 平均比重: " + sumW5Value/w5Values.size()
                                     + " 最大道路长度 : " + maxRoadLength + " 最小道路长度: " + minRoadLength + " 平均道路长度: " + (float)sumRoadLength/roadlengths.size()
                                     + " 最大道路速度: " + maxSpeedLimit + " 最小道路速度: " + minSpeedLimit + " 平均道路速度: " + (float)sumSpeedLimit/speedLimits.size());

                    tempTime = time;
                    roadlengths.clear();
                    speedLimits.clear();
                    capacitys.clear();
                    w5Values.clear();

                    roadlengths.add(roadBean.roadLength);
                    speedLimits.add(roadBean.speedLimit);
                    capacitys.add((sum*1.0 / roadBean.roadNums));
                    if(w5Value != Double.MIN_VALUE){
                        w5Values.add(w5Value);
                    }
                }
            }

            // 修改结束

            //roadBean.crossBusiness = sum * 1.0 / roadBean.roadNums;
            if (roadBean.relativeRoadBlock < 0) {
                System.out.println("出现权重为负的情况: " + roadBean.relativeRoadBlock);
            }
        }
    }


    /**
     * 计算道路上的等待车辆和总的车辆个数
     *
     * @param roadBean 需要统计的道路
     * @return 返回数组  长度为2  AllCarSize  CarRunningInTheCar
     */
    public static int[] FindTheWaitingCarInLane(RoadBean roadBean) {

        int AllCarSize = 0;

        int CarRunningInTheCar = 0;
        for (List<CarBean> carBeans : roadBean.carBeanQueues) {
            AllCarSize += carBeans.size();
            for (int j = carBeans.size() - 1; j >= 0; j--) {
                CarBean carBean = carBeans.get(j);
                if (carBean.isWaiting) CarRunningInTheCar++;
                else break;
            }
        }
        return new int[]{AllCarSize, CarRunningInTheCar};
    }


    /**
     * 计算相对最短路径
     *
     * @param verTexId 表示交叉路口的索引id  待计算verTexId 到其它所有点的最短距离
     * @param path     路径数据
     * @param visited  是否存在最短路径
     */

    /*public static double[] ShortestPath_DJSTR(int preVerTexId, int verTexId, int path[][], int[] visited, int destCrossIndex) {

        //设置 preVerTexId--->verTexId 为空
        //设置当前道路的反向道路不可用 不允许调头
        boolean isSet = false;
        RoadBean reverseRoad = null;
        if (preVerTexId != -1)
            for (ArcBox q = Graph[verTexId].firstout; q != null; q = q.tlink) {
                if (LocateVex(q.roadBean.endCross) == preVerTexId && q.roadBean.visible) {
                    q.roadBean.visible = false;
                    reverseRoad = q.roadBean;
                    isSet = true;

                    break;
                }
            }


        double d[] = new double[Graph.length];
        //int[] visited = new int[Graph.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = Double.MAX_VALUE;
            //表示最短路径没有发现
            visited[i] = 0;
        }
        ArcBox next = Graph[verTexId].firstout;
        //表示此条边可用
        while (next != null) {//&& (preRoadBean == null || preRoadBean.roadId != next.roadBean.roadId)
            if (!next.roadBean.visible) {
                next = next.tlink;
                continue;
            }
            RoadBean roadBean = next.roadBean;
            int loc = LocateVex(roadBean.endCross);
            d[loc] = roadBean.relativeRoadBlock;
            //保留路径信息
            path[loc][0] = verTexId;
            path[loc][1] = loc;
            //目前还未知
            path[loc][2] = -1;
            //尾指针相同
            next = next.tlink;
        }
        //找出最短路径
        visited[verTexId] = 1;
        d[verTexId] = 0;
        int k = -1;
        // verTexNodes.length-1个点
        for (int i = 1; i < Graph.length; i++) {

            double min = Double.MAX_VALUE;

            for (int j = 0; j < Graph.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j] < min) {
                    k = j;
                    min = d[j];
                }
            }

            if (min == Double.MAX_VALUE) return d;

            visited[k] = 1;

            //设置当前调头边可用
            if (i == 1 && isSet) reverseRoad.visible = true;

            //提前结束避免时间损耗
            if (k == destCrossIndex) return d;
            ArcBox p = Graph[k].firstout;
            while (p != null) {//&& (preRoadBean == null || preRoadBean.roadId != p.roadBean.roadId)
                RoadBean roadBean = p.roadBean;

                if (!roadBean.visible) {
                    p = p.tlink;
                    continue;
                }

                int loc = LocateVex(roadBean.endCross);
                if (visited[loc] == 0 && d[loc] > d[k] + roadBean.relativeRoadBlock) {
                    d[loc] = d[k] + roadBean.relativeRoadBlock;
                    int t = 0;
                    while (path[k][t] != -1) {
                        path[loc][t] = path[k][t];
                        t++;
                    }
                    path[loc][t] = loc;
                    path[loc][t + 1] = -1;
                }
                p = p.tlink;

            }
        }
        return d;
    }*/


    //计算绝对的最短距离 忽视道路上车辆的拥堵情况
/*
    public static double[] ShortestPath_DJSTRAbSolute(int preVerTexId, int verTexId, int path[][], int[] visited, int destCrossIndex) {

        //设置 preVerTexId--->verTexId 为空
        //设置当前道路的反向道路不可用 不允许调头
        int novisibleRoadId = -1;
        if (preVerTexId != -1)
            for (ArcBox q = Graph[verTexId].firstout; q != null; q = q.tlink) {
                if (LocateVex(q.roadBean.endCross) == preVerTexId) {
                    novisibleRoadId = q.roadBean.roadId;
                    break;
                }
            }

        double d[] = new double[Graph.length];

        //int[] visited = new int[Graph.length];

        for (int i = 0; i < d.length; i++) {
            d[i] = Double.MAX_VALUE;
            //表示最短路径没有发现
            visited[i] = 0;
        }

        ArcBox next = Graph[verTexId].firstout;
        //表示此条边可用
        while (next != null) {
            if (novisibleRoadId != -1 && next.roadBean.roadId == novisibleRoadId) {
                next = next.tlink;
                continue;
            }
            RoadBean roadBean = next.roadBean;
            int loc = LocateVex(roadBean.endCross);
            //d[loc] = roadBean.relativeRoadBlock;
            d[loc] = Graph[loc].crossBusiness;
            //保留路径信息
            path[loc][0] = verTexId;
            path[loc][1] = loc;
            //目前还未知
            path[loc][2] = -1;
            //尾指针相同
            next = next.tlink;
        }

        //找出最短路径
        visited[verTexId] = 1;
        d[verTexId] = 0;
        int k = -1;
        // verTexNodes.length-1个点
        for (int i = 1; i < Graph.length; i++) {
            double min = Double.MAX_VALUE;
            for (int j = 0; j < Graph.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j] < min) {
                    k = j;
                    min = d[j];
                }
            }
            if (min == Double.MAX_VALUE) return d;
            visited[k] = 1;
            if (k == destCrossIndex) return d;
            ArcBox p = Graph[k].firstout;
            while (p != null) {//&& (preRoadBean == null || preRoadBean.roadId != p.roadBean.roadId)
                RoadBean roadBean = p.roadBean;
              */
/*  if (novisibleRoadId != -1 && LocateVex(roadBean.startCross) == verTexId && LocateVex(roadBean.endCross) == preVerTexId) {
                    p = p.tlink;
                    continue;
                }*//*


                int loc = LocateVex(roadBean.endCross);
                if (visited[loc] == 0 && d[loc] > d[k] + Graph[loc].crossBusiness) {//visited[loc] == 0 && d[loc] > d[k] + roadBean.roadLength Graph[loc].crossBusiness
                    d[loc] = d[k] +Graph[loc].crossBusiness;//d[loc] = d[k] + Graph[loc].crossBusiness;
                    int t = 0;
                    while (path[k][t] != -1) {
                        path[loc][t] = path[k][t];
                        t++;
                    }
                    path[loc][t] = loc;
                    path[loc][t + 1] = -1;
                }
                p = p.tlink;
            }
        }

        return d;
    }
*/





    /**
     * 计算相对最短路径
     * <p>
     * //@param verTexId 表示交叉路口的索引id  待计算verTexId 到其它所有点的最短距离
     * //@param path     路径数据
     * //@param visited  是否存在最短路径
     */

    public static void ShortestPath_DJSTR(int preVerTexId, int verTexId, int path[][], int[] visited, int destCrossIndex) {

        //设置 preVerTexId--->verTexId 为空
        //设置当前道路的反向道路不可用 不允许调头

        TurnDistanceBean d[] = new TurnDistanceBean[Graph.length];
        //int[] visited = new int[Graph.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = new TurnDistanceBean(Double.MAX_VALUE, -1);
            //d[i].distance = Double.MAX_VALUE;
            //表示最短路径没有发现
            visited[i] = 0;
        }
        ArcBox next = Graph[verTexId].firstout;
        //表示此条边可用
        while (next != null) {//&& (preRoadBean == null || preRoadBean.roadId != next.roadBean.roadId)
            if (!next.roadBean.visible || LocateVex(next.roadBean.endCross) == preVerTexId) {
                next = next.tlink;
                continue;
            }
            RoadBean roadBean = next.roadBean;
            int loc = LocateVex(roadBean.endCross);
            //保存距离和此条边对应的起点id
            d[loc].distance = roadBean.relativeRoadBlock;
            d[loc].roadId = roadBean.roadId;

            //保留路径信息
            path[loc][0] = verTexId;
            path[loc][1] = loc;
            //目前还未知
            path[loc][2] = -1;
            //尾指针相同
            next = next.tlink;
        }
        //找出最短路径
        visited[verTexId] = 1;
        d[verTexId].distance = 0;
        int k = -1;
        // verTexNodes.length-1个点
        for (int i = 1; i < Graph.length; i++) {

            double min = Double.MAX_VALUE;

            for (int j = 0; j < Graph.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j].distance < min) {
                    k = j;
                    min = d[j].distance;
                }
            }

            if (min == Double.MAX_VALUE) return;

            visited[k] = 1;

            //提前结束避免时间损耗
            if (k == destCrossIndex) return;
            ArcBox p = Graph[k].firstout;
            while (p != null) {//&& (preRoadBean == null || preRoadBean.roadId != p.roadBean.roadId)
                RoadBean roadBean = p.roadBean;

                int loc = LocateVex(roadBean.endCross);

                if (!roadBean.visible || roadBean.roadId == d[k].roadId) {
                    p = p.tlink;
                    continue;
                }

                if (visited[loc] == 0 && d[loc].distance > d[k].distance + roadBean.relativeRoadBlock) {
                    d[loc].distance = d[k].distance + roadBean.relativeRoadBlock;
                    d[loc].roadId = roadBean.roadId;
                    int t = 0;
                    while (path[k][t] != -1) {
                        path[loc][t] = path[k][t];
                        t++;
                    }
                    path[loc][t] = loc;
                    path[loc][t + 1] = -1;
                }
                p = p.tlink;

            }
        }
    }


    //计算绝对的最短距离 忽视道路上车辆的拥堵情况
    public static void ShortestPath_DJSTRAbSolute(int preVerTexId, int verTexId, int path[][], int[] visited, int destCrossIndex) {

        //设置 preVerTexId--->verTexId 为空
        //设置当前道路的反向道路不可用 不允许调头

        TurnDistanceBean d[] = new TurnDistanceBean[Graph.length];

        for (int i = 0; i < d.length; i++) {

            d[i] = new TurnDistanceBean(Double.MAX_VALUE, -1);
            //表示最短路径没有发现
            visited[i] = 0;
        }

        ArcBox next = Graph[verTexId].firstout;
        //表示此条边可用
        while (next != null) {
            if (LocateVex(next.roadBean.endCross) == preVerTexId) {
                next = next.tlink;
                continue;
            }
            RoadBean roadBean = next.roadBean;
            int loc = LocateVex(roadBean.endCross);
            d[loc].distance = roadBean.roadLength+roadBean.penaltyWeight;
            d[loc].roadId = roadBean.roadId;
            //d[loc].distance = Graph[loc].crossBusiness;
            //保留路径信息
            path[loc][0] = verTexId;
            path[loc][1] = loc;
            //目前还未知
            path[loc][2] = -1;
            //尾指针相同
            next = next.tlink;
        }

        //找出最短路径
        visited[verTexId] = 1;
        d[verTexId].distance = 0;


        int k = -1;
        // verTexNodes.length-1个点
        for (int i = 1; i < Graph.length; i++) {
            double min = Double.MAX_VALUE;
            for (int j = 0; j < Graph.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j].distance < min) {
                    k = j;
                    min = d[j].distance;

                }
            }
            if (min == Double.MAX_VALUE) return;
            visited[k] = 1;
            if (k == destCrossIndex) return;
            ArcBox p = Graph[k].firstout;
            while (p != null) {
                RoadBean roadBean = p.roadBean;
                //调头判断
                if (d[k].roadId == roadBean.roadId) {
                    p = p.tlink;
                    continue;
                }

                int loc = LocateVex(roadBean.endCross);
                if (visited[loc] == 0 && d[loc].distance > d[k].distance + roadBean.roadLength+roadBean.penaltyWeight) {//visited[loc] == 0 && d[loc] > d[k] + roadBean.roadLength Graph[loc].crossBusiness
                    d[loc].distance = d[k].distance + roadBean.roadLength+roadBean.penaltyWeight; ;//d[loc] = d[k] + Graph[loc].crossBusiness;
                    d[loc].roadId = roadBean.roadId;
                    int t = 0;
                    while (path[k][t] != -1) {
                        path[loc][t] = path[k][t];
                        t++;
                    }
                    path[loc][t] = loc;
                    path[loc][t + 1] = -1;
                }
                p = p.tlink;
            }
        }

        return;
    }


    private static int FindTheDirection(int startCrossIndex, int nStartCrossIndex, int endCrossIndex) {
        //startCrossIndex--->nStartCrossIndex--->endCrossIndex

        int directionFirst = -1;
        int directionSecond = -1;
        for (ArcBox p = Graph[nStartCrossIndex].firstin; p != null; p = p.hlink) {
            if (LocateVex(p.roadBean.startCross) == startCrossIndex) {
                directionFirst = p.headDirection;
                break;
            }
        }

        if (directionFirst == -1) throw new RuntimeException("内部数据异常!");


        for (ArcBox q = Graph[nStartCrossIndex].firstout; q != null; q = q.tlink) {
            if (LocateVex(q.roadBean.endCross) == endCrossIndex) {
                directionSecond = q.tailDirection;
                break;
            }
        }

        if (directionSecond == -1) throw new RuntimeException("内部数据异常");


        int directionFirstCopy = directionFirst;


        directionFirstCopy = (directionFirst + 1) % 4;

        if (directionFirstCopy == directionSecond) return Constant.LEFT;

        directionFirstCopy = (directionFirst - 1) < 0 ? (directionFirst - 1 + 4) % 4 : (directionFirst - 1) % 4;

        if (directionFirstCopy == directionSecond) return Constant.RIGHT;

        directionFirstCopy = (directionFirst - 2) < 0 ? (directionFirst - 2 + 4) % 4 : (directionFirst - 2) % 4;
        if (directionFirstCopy == directionSecond) return Constant.STRAIGHT;


        throw new RuntimeException("内部数据异常!");

    }


    /**
     * 判断三个交叉入口的方向是不是direction
     *
     * @param startCrossIndex  开始交叉入口id
     * @param nstartCrossIndex 中间交叉入口id
     * @param endCrossIndex    结束交叉入口id
     * @param direction        方向
     *                         startCrossIndex---> nstartCrossIndex--->endCrossIndex
     * @return
     */
    /*private static boolean isInTheSameDirection(int startCrossIndex, int nstartCrossIndex, int endCrossIndex, int direction) {


        int firstDirection = -1;
        int secondDirection = -1;


        for (ArcBox p = Graph[nstartCrossIndex].firstin; p != null; p = p.hlink) {

            if (LocateVex(p.roadBean.startCross) == startCrossIndex) {
                firstDirection = p.headDirection;
                break;
            }
        }

        for (ArcBox p = Graph[nstartCrossIndex].firstout; p != null; p = p.tlink) {

            if (LocateVex(p.roadBean.endCross) == endCrossIndex) {
                secondDirection = p.tailDirection;
                break;
            }
        }

        if (firstDirection == -1 || secondDirection == -1) throw new RuntimeException("内部数据错误");

        int tempt = firstDirection;
        if (direction == Constant.STRAIGHT) {
            tempt = firstDirection - 2 < 0 ? (4 + firstDirection - 2) % 4 : (firstDirection - 2) % 4;

            if (tempt == secondDirection) return true;
            else return false;
        }


        if (direction == Constant.LEFT) {

            tempt = (firstDirection + 1) % 4;

            if (tempt == secondDirection) return true;
            else return false;
        }

        throw new RuntimeException("内部数据错误");

    }*/

}
