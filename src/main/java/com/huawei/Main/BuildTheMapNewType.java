package com.huawei.Main;

import com.huawei.beans.CarBean;
import com.huawei.beans.Constant;
import com.huawei.beans.CrossBean;
import com.huawei.beans.RoadBean;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 采用十字链表的构建图
 */
public class BuildTheMapNewType {

    public static List<CrossBean> crossBeanList = new ArrayList<>();
    public static List<RoadBean> roadBeanList = new ArrayList<>();
    public static List<CarBean> carBeanList = new ArrayList<>();
    public static VexNode[] Graph;

    static {
        readCrossBeanList();
        readRoadBeanList();
        readCarBeanList();
        Collections.sort(carBeanList);
        Graph = BuildTheVertecNodesMap();
    }


    public static List<CrossBean> readCrossBeanList() {
        String str = null;
        try (//读取其中的road.txt文件
             InputStream resourceAsStream = BuildTheMapOldType.class.getClassLoader().getResourceAsStream("Cross.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            Pattern compile = Pattern.compile("\\((?<crossId>[0-9]+),[ ]+(?<upRoad>[-]?[0-9]+),[ ]+(?<rigthRoad>[-]?[0-9]+),[ ]+(?<downRoad>[-]?[0-9]+),[ ]+(?<leftRoad>[-]?[0-9]+)\\)");
            while ((str = bufferedReader.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                } else {
                    Matcher matcher = compile.matcher(str);
                    if (matcher.find()) {
                        int crossId = Integer.parseInt(matcher.group("crossId"));
                        int upRoad = Integer.parseInt(matcher.group("upRoad"));
                        int rigthRoad = Integer.parseInt(matcher.group("rigthRoad"));
                        int downRoad = Integer.parseInt(matcher.group("downRoad"));
                        int leftRoad = Integer.parseInt(matcher.group("leftRoad"));
                        crossBeanList.add(new CrossBean(crossId, upRoad, rigthRoad, downRoad, leftRoad));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return crossBeanList;

    }

    public static List<RoadBean> readRoadBeanList() {
        String str = null;
        try (//读取其中的road.txt文件
             InputStream resourceAsStream = BuildTheMapOldType.class.getClassLoader().getResourceAsStream("Road.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            Pattern compile = Pattern.compile("\\((?<roadId>[0-9]+),[ ]+(?<roadLength>[0-9]+),[ ]+(?<speedLimit>[0-9]+),[ ]+(?<roadNums>[0-9]+),[ ]+(?<startCross>[0-9]+),[ ]+(?<endCross>[0-9]+),[ ]+(?<isBothWay>[0-9]+)\\)");
            while ((str = bufferedReader.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                } else {
                    Matcher matcher = compile.matcher(str);
                    if (matcher.find()) {
                        int roadId = Integer.parseInt(matcher.group("roadId"));
                        int roadLength = Integer.parseInt(matcher.group("roadLength"));
                        int speedLimit = Integer.parseInt(matcher.group("speedLimit"));
                        int roadNums = Integer.parseInt(matcher.group("roadNums"));
                        int startCross = Integer.parseInt(matcher.group("startCross"));
                        int endCross = Integer.parseInt(matcher.group("endCross"));
                        int isBothWay = Integer.parseInt(matcher.group("isBothWay"));
                        roadBeanList.add(new RoadBean(roadId, roadLength, speedLimit, roadNums, startCross, endCross, isBothWay));
                        if (isBothWay == 1) {
                            roadBeanList.add(new RoadBean(roadId, roadLength, speedLimit, roadNums, endCross, startCross, isBothWay));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return roadBeanList;
    }

    public static List<CarBean> readCarBeanList() {

        String str = null;
        try (//读取其中的road.txt文件
             InputStream resourceAsStream = BuildTheMapOldType.class.getClassLoader().getResourceAsStream("Car.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            Pattern compile = Pattern.compile("\\((?<carId>[0-9]+),(?<startCrossId>[0-9]+),(?<endCrossId>[0-9]+),(?<maxSpeed>[0-9]+),(?<time>[0-9]+)\\)");
            while ((str = bufferedReader.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                } else {
                    Matcher matcher = compile.matcher(str);
                    if (matcher.find()) {
                        int carId = Integer.parseInt(matcher.group("carId"));
                        int startCrossId = Integer.parseInt(matcher.group("startCrossId"));
                        int endCrossId = Integer.parseInt(matcher.group("endCrossId"));
                        int maxSpeed = Integer.parseInt(matcher.group("maxSpeed"));
                        int time = Integer.parseInt(matcher.group("time"));
                        carBeanList.add(new CarBean(carId, startCrossId, endCrossId, maxSpeed, time, false, crossBeanList.size()));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return carBeanList;
    }


    private static VexNode[] BuildTheVertecNodesMap() {
        VexNode[] vertexNodes = new VexNode[crossBeanList.size()];

        //先初始化数据
        for (int i = 0; i < vertexNodes.length; i++) {
            CrossBean crossBean = crossBeanList.get(i);
            VexNode vertexNode = new VexNode(crossBean.crossId);
            vertexNodes[i] = vertexNode;
        }

        //图构建
        //生成边数
        for (int e = roadBeanList.size() - 1; e >= 0; e--) {
            RoadBean roadBean = roadBeanList.get(e);
            int startCross = roadBean.startCross;
            int endCross = roadBean.endCross;
            int i = LocateVex(vertexNodes, startCross);
            int j = LocateVex(vertexNodes, endCross);


            int tailDirection = -1;

            int headDirection = -1;
            for (CrossBean crossBean : crossBeanList) {

                //数据方向已经找到
                if (tailDirection != -1 && headDirection != -1) break;

                if (tailDirection != -1 && crossBean.crossId == startCross) {

                    if (crossBean.upRoadId != -1 && crossBean.upRoadId == roadBean.roadId) {
                        tailDirection = Constant.UPD;
                    }

                    if (tailDirection != -1 && crossBean.rightRoadId != -1 && crossBean.rightRoadId == roadBean.roadId) {
                        tailDirection = Constant.RIGHTD;
                    }

                    if (tailDirection != -1 && crossBean.downRoadId != -1 && crossBean.downRoadId == roadBean.roadId) {
                        tailDirection = Constant.DOWND;
                    }

                    if (tailDirection != -1 && crossBean.leftRoadId != -1 && crossBean.leftRoadId == roadBean.roadId) {
                        tailDirection = Constant.LEFTD;
                    }
                }

                if (headDirection != -1 && crossBean.crossId == endCross) {

                    if (crossBean.upRoadId != -1 && crossBean.upRoadId == roadBean.roadId) {
                        headDirection = Constant.UPD;
                    }

                    if (headDirection != -1 && crossBean.rightRoadId != -1 && crossBean.rightRoadId == roadBean.roadId) {
                        headDirection = Constant.RIGHTD;
                    }

                    if (headDirection != -1 && crossBean.downRoadId != -1 && crossBean.downRoadId == roadBean.roadId) {
                        headDirection = Constant.DOWND;
                    }

                    if (headDirection != -1 && crossBean.leftRoadId != -1 && crossBean.leftRoadId == roadBean.roadId) {
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


    public static void main(String... args) {

        ArrangeTheCar(Graph);
    }

    private static int LocateVex(VexNode[] graph, int crossId) {

        for (int i = 0; i < graph.length; i++) {

            if (graph[i].crossId == crossId) {
                return i;
            }
        }

        throw new RuntimeException("内部数据错误");
    }

    static class ArcBox {
        //该弧的尾部所在的位置
        int tailVex; //表示数组中的索引位置
        //该弧的头部所在位置
        int headVex;

        //以tailVex为中心 headVex所在的位置信息  0 上 1 右  2 下 3 左
        int tailDirection;
        //以head为中心 tailVex所在的位置信息
        int headDirection;

        //弧头相同的节点
        ArcBox hlink;
        //弧尾相同的节点
        ArcBox tlink;
        //此条弧的信息
        RoadBean roadBean;


        ArcBox(int tailVex, int headVex, ArcBox hlink, ArcBox tlink, RoadBean roadBean, int tailDirection, int headDirection) {

            this.tailVex = tailVex;
            this.headVex = headVex;
            this.hlink = hlink;
            this.tlink = tlink;
            this.roadBean = roadBean;
            this.tailDirection = tailDirection;
            this.headDirection = headDirection;
        }


    }

    static class VexNode {

        int crossId;
        //指向该顶点的入弧
        ArcBox firstin;
        //指向该顶点的出弧
        ArcBox firstout;

        VexNode(int crossId) {
            this.crossId = crossId;
        }

    }


    /**
     * @param verTexNodes
     * @param verTexId    表示的是verTexNodes数组下标,而不是crossId
     * @param path
     */
    public static void ShortestPath_DJSTR(VexNode verTexNodes[], int verTexId, int path[][]) {

        int d[] = new int[verTexNodes.length];

        int[] visited = new int[verTexNodes.length];

        for (int i = 0; i < d.length; i++) {
            d[i] = Integer.MAX_VALUE;
            //表示最短路径没有发现
            visited[i] = 0;
        }

        ArcBox next = verTexNodes[verTexId].firstout;
        while (next != null) {
            RoadBean roadBean = next.roadBean;
            int loc = LocateVex(verTexNodes, roadBean.endCross);
            d[loc] = roadBean.roadLength;
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
        for (int i = 1; i < verTexNodes.length; i++) {

            int min = Integer.MAX_VALUE;

            for (int j = 1; j < verTexNodes.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j] < min) {
                    k = j;
                    min = d[j];
                }
            }

            if (min == Integer.MAX_VALUE) return;

            visited[k] = 1;
            ArcBox p = verTexNodes[k].firstout;
            while (p != null) {
                RoadBean roadBean = p.roadBean;

                int loc = LocateVex(verTexNodes, roadBean.endCross);

                if (visited[loc] == 0 && d[loc] > d[k] + roadBean.roadLength) {
                    d[loc] = d[k] + roadBean.roadLength;
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


    /**
     * 车辆是否调度完成
     *
     * @return
     */
    private static boolean isAllFinish() {

        for (int i = 0; i < carBeanList.size(); i++) {
            if (!carBeanList.get(i).isFinish) {

                return false;
            }
        }

        return true;
    }

    /**
     * 对于每辆车判断可能的路径
     */
    private static void ArrangeTheCar(VexNode verTexNodes[]) {
        //最短路径算法 求所有点之间的最短路径
        //当前调度时刻
        int time = 0;

        while (!isAllFinish()) {//依然有车量在运行,没有运行完成


            //时间片
            time++;
            System.out.println(time);
            //标志车的状态  等待和终止
            System.out.println("FlageTheWaitingCar");
            FlageTheWaitingCar();
            System.out.println("RunTheWaitingCar");
            //调度等待车辆
            RunTheWaitingCar(verTexNodes);
            System.out.println("driverAllCarInGarage");
            //运行车库中车辆
            driverAllCarInGarage(verTexNodes, time);
        }

        System.out.println("finish");


    }

    /**
     * 运行等待车辆
     *
     * @param verTexNodes
     */
    private static void RunTheWaitingCar(VexNode[] verTexNodes) {
        //调度等待车辆
        while (!isAllNormalStopStatus()) {
            //按照路口id升序 扫描所有路口
            for (int i = 1; i < verTexNodes.length; i++) {
                //路口对应的出口道路 按照道路id升序扫描   目前还没有排序
                for (ArcBox p = verTexNodes[i].firstin; p != null; p = p.hlink) {
                    RoadBean roadBean = p.roadBean;
                    //调度 当前路口 verTexNodes[i] 的所有入口道路


                    //每条道路上所有的车辆  车道号小的首先调度
                    LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
                    //调度每一条车道
                    //TODO  设置一个循环状态保存
                    int index = 0;

                    while (index < roadBean.roadNums) {

                        //当前道路在 verTexNodes[i]的位置
                        // index 表示当前的队列集合
                        LinkedList<CarBean> carBeanQueue = carBeanQueues[roadBean.index];
                        //取队列头部元素
                        CarBean carBean = carBeanQueue.peek();

                        if (carBean == null || !carBean.isWaiting) {//等待状态才调度
                            roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                            index++;
                            continue;
                        } else {
                            //存在一次不满足条件就重置 index
                            index = 0;
                        }

                        //判断当前车的方向是否能调度
                        // TODO  carBean.index 等待设置
                        int direction = carBean.direction[carBean.index];

                        //新跳转的对应边的
                        int eroadId = carBean.visitedEdge[carBean.index + 1];

                        //跳转到的道路
                        RoadBean nextRoad = null;

                        for (ArcBox q = verTexNodes[i].firstout; q != null; q = q.tlink) {

                            if (q.roadBean.roadId == eroadId) {
                                nextRoad = q.roadBean;
                                break;
                            }
                        }
                        if (nextRoad == null) throw new RuntimeException("内部数据错误nextRoad不能为空！");


                        //获得转弯之后的那个道路
                        if (direction == Constant.STRAIGHT) {
                            //转弯成功
                            if (waitingCarChangeDirection(i, roadBean, carBeanQueue, carBean, nextRoad)) continue;
                            else break;//转弯不成功
                        } else if (direction == Constant.LEFT) {
                            //直行道路
                            RoadBean strRoadBean = null;
                            //新方向
                            int newDirection = p.headDirection - 1 < 0 ? (4 + p.headDirection - 1) % 4 : (p.headDirection - 1) % 4;
                            for (ArcBox q = verTexNodes[i].firstin; q != null; q = q.hlink) {
                                if (q.headDirection == newDirection) { //找出直行对应的道路
                                    strRoadBean = q.roadBean;
                                    break;
                                }
                            }

                            if (strRoadBean != null && BlockingFlages(carBeanQueue, strRoadBean, Constant.STRAIGHT))
                                break;
                            //判断存在问题
                            //是否存在阻挡
                            //不存在阻挡
                            if (waitingCarChangeDirection(i, roadBean, carBeanQueue, carBean, nextRoad)) continue;
                            else break;
                        } else if (direction == Constant.RIGHT) {
                            RoadBean strRoadBean = null;
                            RoadBean leftRoadBean = null;
                            int strDirection = (p.headDirection + 1) % 4;
                            int leftDirection = p.headDirection - 2 < 0 ? (4 + p.headDirection - 2) % 4 : (p.headDirection - 2) % 4;
                            for (ArcBox q = verTexNodes[i].firstin; q != null; q = q.hlink) {
                                if (strRoadBean == null && leftRoadBean == null) break;
                                if (q.headDirection == strDirection) { //找出直行对应的道路
                                    strRoadBean = q.roadBean;
                                }
                                if (q.headDirection == leftDirection) {
                                    leftRoadBean = q.roadBean;
                                }
                            }
                            //找出直行对应的道路
                            if (strRoadBean != null && BlockingFlages(carBeanQueue, strRoadBean, Constant.STRAIGHT))
                                break;
                            //找出左转对应的道路
                            if (leftRoadBean != null && BlockingFlages(carBeanQueue, leftRoadBean, Constant.LEFT))
                                break;
                            //如果都不存在  开始调度
                            if (waitingCarChangeDirection(i, roadBean, carBeanQueue, carBean, nextRoad)) continue;
                            else break;

                        } else {
                            throw new RuntimeException("方向有问题!!!");
                        }
                    }
                }
            }
        }
    }

    private static boolean BlockingFlages(LinkedList<CarBean> carBeanQueue, RoadBean strRoadBean, int direction) {
        boolean isBlock = false;
        for (int k = 0; k < carBeanQueue.size(); k++) {

            if (strRoadBean.index >= strRoadBean.roadNums) {
                isBlock = false;
                break;
            }
            LinkedList<CarBean> carStrBeanQueue = strRoadBean.carBeanQueues[strRoadBean.index];
            if (carStrBeanQueue.size() == 0) {//为0
                strRoadBean.index = strRoadBean.index + 1;

            } else {//不为空
                CarBean peek = carBeanQueue.peek();
                //存在阻挡
                if (peek.direction[peek.index] == direction) {
                    isBlock = true;
                    break;
                }

            }
        }
        return isBlock;
    }

    private static boolean waitingCarChangeDirection(int i, RoadBean roadBean, LinkedList<CarBean> carBeanQueue, CarBean carBean, RoadBean nextRoad) {
        //判断即将转到的新道路情况
        LinkedList<CarBean>[] carBeanQueuesnext = nextRoad.carBeanQueues;
        //判断是否调度成功
        boolean isSuccess = false;


        int v1 = carBean.currSpeed;

        //v2可能的速度
        int v2 = Math.min(carBean.maxSpeed, nextRoad.speedLimit);

        int s1 = carBean.s1;
        //等待初始化
        int s2 = -1;

        for (int j = 0; j < carBeanQueuesnext.length; j++) {
            CarBean carBeanNext = carBeanQueuesnext[j].peekLast();
            if (carBeanNext == null) {//此队列为空


                //调度成功  更新相应数据
                //存在速度导致调度不成功
                carBean.isWaiting = false;
                if (v2 - s1 <= 0) {//说明不能通过入口
                    carBean.s1 = 0;
                    //更新当前车道上所有等待车辆信息
                    for (int k = carBeanQueue.size() - 2; k >= 0; k--) {
                        CarBean carBeanChange = carBeanQueue.get(k);
                        if (!carBeanChange.isWaiting) {
                            break;
                        }
                        CarBean precarBeanChage = carBeanQueue.get(k + 1);
                        int distance = carBeanChange.s1 - precarBeanChage.s1 - 1;
                        //当前速度道路限速和终止状态的距离
                        int maxValue = Math.min(Math.min(roadBean.speedLimit, carBeanChange.maxSpeed), distance);
                        carBeanChange.s1 = carBeanChange.s1 - maxValue;
                        carBeanChange.isWaiting = false;
                    }

                } else {

                    //查找当前道路的下一条车道
                    roadBean.index = (roadBean.index + 1) % roadBean.roadNums;


                    isSuccess = true;
                    carBean.currentRoadBean = nextRoad;
                    //s2最大运行的距离
                    int maxS2 = v2 - s1;
                    //默认只能通过一个道路 数据为正
                    carBean.s1 = nextRoad.roadLength - maxS2;
                    carBean.currSpeed = v2;
                    //更新方向索引
                    carBean.index++;
                    //从原来队列中删除元素
                    carBeanQueue.poll();
                    //保存元素到最新队列
                    carBeanQueuesnext[i].add(carBean);

                }
                //不需要再调度
                break;
            }


            if (carBeanNext.s1 >= nextRoad.roadLength - 1) {//此车占着位置
                if (carBeanNext.isWaiting) {//车道正在等待,说明当前车无法调度 依然处于等待状态
                    break;
                } else {//道路下一个车道 说明车到已满. 需要换一条车道容纳当前车辆
                    continue;
                }
            } else {//此车有空位

                s2 = nextRoad.roadLength - carBeanNext.s1 - 1;

                //下一道路上车是等待状态
                if (carBeanNext.isWaiting) {
                    //设置数据更新状态  看是否是等待状态或者进一步的正常终止

                    //不可以通过入口
                    if (v2 - carBean.s1 <= 0) {//到当前车道最前位置

                        carBean.s1 = 0;
                        carBean.isWaiting = false;
                        //更新当前车道上所有等待车辆信息
                        for (int k = carBeanQueue.size() - 2; k >= 0; k--) {
                            CarBean carBeanChange = carBeanQueue.get(k);
                            if (!carBeanChange.isWaiting) {
                                break;
                            }
                            CarBean precarBeanChage = carBeanQueue.get(k + 1);
                            int distance = carBeanChange.s1 - precarBeanChage.s1 - 1;
                            //当前速度道路限速和终止状态的距离   最小值最为最大行驶速度
                            int maxValue = Math.min(roadBean.speedLimit, distance);
                            carBeanChange.s1 = carBeanChange.s1 - maxValue;
                            carBeanChange.isWaiting = false;
                        }

                        break;
                    }

                    //不可通过  可能存在死锁
                    if (v2 - carBean.s1 > s2) {//超过允许通行长度   等待再次调度
                        break;
                    } else {//可以通过
                        isSuccess = true;
                        carBean.s1 = nextRoad.roadLength - (v2 - carBean.s1);
                        carBean.currentRoadBean = nextRoad;
                        carBean.currSpeed = v2;
                        carBeanQueue.poll();
                        carBeanQueuesnext[i].add(carBean);
                        carBean.isWaiting = false;
                        //查找当前道路的下一条车道
                        roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                        break;

                    }
                } else {//终止状态
                    // carBean最多允许运行的距离
                    //当前道路运行的长度
                    assert carBeanNext.isWaiting == false;
                    v2 = Math.min(v2, carBean.s1 + nextRoad.roadLength - carBeanNext.s1 - 1);
                    //共同使用到数据
                    carBean.isWaiting = false;
                    if (v2 - carBean.s1 <= 0) {//不能通过

                        carBean.s1 = 0;
                        //更新当前车道上所有等待车辆信息
                        for (int k = carBeanQueue.size() - 2; k >= 0; k--) {
                            CarBean carBeanChange = carBeanQueue.get(k);
                            if (!carBeanChange.isWaiting) {
                                break;
                            }
                            CarBean precarBeanChage = carBeanQueue.get(k + 1);
                            int distance = carBeanChange.s1 - precarBeanChage.s1 - 1;
                            //当前速度道路限速和终止状态的距离   最小值最为最大行驶速度
                            int maxValue = Math.min(roadBean.speedLimit, distance);
                            carBeanChange.s1 = carBeanChange.s1 - maxValue;
                            carBeanChange.isWaiting = false;
                        }

                        break;
                    }

                    //能通过
                    isSuccess = true;
                    carBean.s1 = nextRoad.roadLength - (v2 - carBean.s1);
                    carBean.currentRoadBean = nextRoad;
                    carBean.currSpeed = v2;
                    carBeanQueue.poll();
                    carBeanQueuesnext[i].add(carBean);
                    //查找当前道路的下一条车道
                    roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                }
            }
        }
        if (isSuccess) {//调度给定道路的某条车道


            //调度当前车道  只调度不出路口的车辆
            for (int k = 0; k <= carBeanQueue.size() - 1; k++) {

                CarBean carBeanCurr = carBeanQueue.get(k);

                CarBean precarBeanCurr = k != carBeanQueue.size() - 1 ? carBeanQueue.get(k + 1) : null;

                if (!carBeanCurr.isWaiting) break;
                int currSpeed = Math.min(roadBean.speedLimit, carBeanCurr.maxSpeed);
                //不需要调整 存在出入口
                if (precarBeanCurr == null && currSpeed > carBeanCurr.s1) break;

                int distance = precarBeanCurr == null ? carBeanCurr.s1 : roadBean.roadLength - precarBeanCurr.s1 - 1;
                currSpeed = Math.min(currSpeed, distance);
                carBeanCurr.currSpeed = currSpeed;
                carBeanCurr.s1 = carBeanCurr.s1 - currSpeed;
                carBeanCurr.isWaiting = false;

                //查找当前道路的下一条车道
                roadBean.index = (roadBean.index + 1) % roadBean.roadNums;

            }
        }
        return isSuccess;
    }


    /**
     * 标识一个时间片后等待调度车辆和终止状态车辆
     */
    private static void FlageTheWaitingCar() {
        //扫描道路中的每一条车道
        for (int i = 0; i < roadBeanList.size(); i++) {//总共的道路数
            //扫描所有的车道
            RoadBean roadBean = roadBeanList.get(i);
            LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
            //运行车辆并标注车的状态
            for (LinkedList<CarBean> carBeans : carBeanQueues) {
                //每一个车道中所有车辆信息   从最早进入的车到最晚进入的车
                for (int index = 0; index <= carBeans.size() - 1; index++) {
                    CarBean carBean = carBeans.get(index);
                    //carBean 后一辆车
                    CarBean preBean = index == 0 ? null : carBeans.get(index - 1);

                    //当前车距离上一辆车的距离    如果当前车没有上一辆车  直接是到尽头的距离
                    int distance = index == 0 ? carBean.s1 : carBean.s1 - preBean.s1 - 1;

                    //之前车辆的速度   如果之前车辆是等待状态 那么当前车以min(道路最大限速,车辆最大速度)   否则 当前车min(道路最大限速,车辆最大速度,两辆车距离之差)
                    int preCarMaxSpeed = index == 0 ? roadBean.speedLimit : preBean.isWaiting ? roadBean.speedLimit : distance;
                    //求出当前车的速度
                    carBean.currSpeed = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), preCarMaxSpeed);


                    if (carBean.currSpeed > distance) {//等待转弯调度

                        //结束状态
                        if (carBean.preVisted[carBean.index+2] == -1) {
                            carBean.isWaiting = false;
                            carBean.isFinish = true;
                            carBeans.poll();
                            index--;
                            continue;
                        }


                        if (index == 0) {//当前车是最前车
                            carBean.isWaiting = true;
                        } else {
                            //如果当前车的前一辆车是等待状态
                            if (preBean.isWaiting) {
                                carBean.isWaiting = true;
                            } else {//如果当前车的上一辆车已经终止
                                //在上一辆车终止的前一个位置
                                carBean.s1 = preBean.s1 + 1;
                                carBean.isWaiting = false;
                            }
                        }
                    } else {//直接终止状态 不需要转弯
                        carBean.s1 -= carBean.currSpeed;
                        carBean.isWaiting = false;
                        //TODO  数据可能有问题
                        if (carBean.preVisted[carBean.index + 2] == -1) {
                            carBean.isFinish = true;
                            carBeans.poll();
                            index--;
                        }
                    }
                }
            }
        }
    }


    //开启车库中的车
    private static void driverAllCarInGarage(VexNode[] verTexNodes, int time) {

        //启动每一个未开启的车辆
        for (int k = 0; k < carBeanList.size(); k++) {
            CarBean carBeanr = carBeanList.get(k);
            //车没有调度完成
            if (!carBeanr.isFinish && !carBeanr.isStart) {

                //调度没有运行的车辆
                CarBean carBeanNotRunning = carBeanList.get(k);
                //没有到调度时间
                if (carBeanNotRunning.startTime < time) continue;
                int[][] path = new int[Graph.length][Graph.length];
                //到达调度时间
                ShortestPath_DJSTR(verTexNodes, LocateVex(verTexNodes, carBeanNotRunning.startBean), path);


                //可能到达的第一个地点
                int dest = path[LocateVex(verTexNodes, carBeanNotRunning.endBean)][1];

                for (ArcBox p = verTexNodes[LocateVex(verTexNodes, carBeanNotRunning.startBean)].firstout; p != null; p = p.tlink) {

                    //找到相应的路径信息
                    if (p.roadBean.endCross == dest) {

                        LinkedList<CarBean>[] carBeanQueues = p.roadBean.carBeanQueues;
                        //道路是否允许车通过
                        for (int q = 0; q < carBeanQueues.length; q++) {

                            //道路中的每一条车道
                            LinkedList<CarBean> carBeanQueue = carBeanQueues[q];
                            //双端队列
                            //没有车  开启当前车辆
                            if (carBeanQueue == null || carBeanQueue.size() == 0) {
                                carBeanNotRunning.isStart = true;
                                carBeanNotRunning.currentRoadBean = p.roadBean;
                                carBeanNotRunning.startBean = time;
                                //默认开启车辆的位置为1
                                carBeanNotRunning.s1 = p.roadBean.roadLength - 1;
                                carBeanNotRunning.index = 0;

                                //设置访问路径  其中的值是交叉路口对应的数组位置
                                int t = 0;
                                while (path[LocateVex(verTexNodes, carBeanNotRunning.endBean)][t] != -1) {
                                    //访问过得所有的点
                                    carBeanNotRunning.preVisted[t] = path[LocateVex(verTexNodes, carBeanNotRunning.endBean)][t];

                                    if (t >= 2) {//计算每个中间点的数据信息
                                        carBeanNotRunning.direction[t - 2] = findTheDirection(carBeanNotRunning.preVisted[t - 2], carBeanNotRunning.preVisted[t - 1], carBeanNotRunning.preVisted[t]);
                                    }

                                    if (t >= 1) {//走过的路径

                                        carBeanNotRunning.visitedEdge[t - 1] = findTheVisitedEdge(carBeanNotRunning.preVisted[t - 1], carBeanNotRunning.preVisted[t]);

                                    }
                                    t++;
                                }
                                //标识结束状态  以-1标识结束
                                carBeanNotRunning.preVisted[t] = -1;
                                carBeanNotRunning.direction[t - 2] = -1;
                                carBeanNotRunning.visitedEdge[t - 1] = -1;


                                //此刻的初始值没有用到
                                carBeanNotRunning.currSpeed = Math.min(carBeanNotRunning.maxSpeed, p.roadBean.speedLimit);

                                carBeanQueue.add(carBeanNotRunning);

                                break;//直接跳出循环
                            } else {  //有车

                                //当前道路最后一辆车
                                CarBean carBean = carBeanQueue.peekLast();
                                if (carBean.s1 == p.roadBean.roadLength - 1) {
                                    continue;
                                } else {//此处不存在车辆阻止当前车辆启动
                                    carBeanNotRunning.isStart = true;
                                    carBeanNotRunning.currentRoadBean = p.roadBean;
                                    //默认开启车辆的位置为1
                                    carBeanNotRunning.s1 = p.roadBean.roadLength - 1;
                                    carBeanNotRunning.startBean = time;

                                    //设置访问路径  其中的值是交叉路口对应的数组位置
                                    int t = 0;
                                    while (path[LocateVex(verTexNodes, carBeanNotRunning.endBean)][t] != -1) {
                                        //访问过得所有的点
                                        carBeanNotRunning.preVisted[t] = path[LocateVex(verTexNodes, carBeanNotRunning.endBean)][t];

                                        if (t >= 2) {//计算每个中间点的数据信息
                                            carBeanNotRunning.direction[t - 2] = findTheDirection(carBeanNotRunning.preVisted[t - 2], carBeanNotRunning.preVisted[t - 1], carBeanNotRunning.preVisted[t]);
                                        }

                                        if (t >= 1) {//走过的路径

                                            carBeanNotRunning.visitedEdge[t - 1] = findTheVisitedEdge(carBeanNotRunning.preVisted[t - 1], carBeanNotRunning.preVisted[t]);

                                        }

                                    }


                                    //标识结束状态  以-1标识结束
                                    carBeanNotRunning.preVisted[t] = -1;
                                    carBeanNotRunning.direction[t - 2] = -1;
                                    carBeanNotRunning.visitedEdge[t - 1] = -1;

                                    //当前车速 最小值  没有用到
                                    carBeanNotRunning.currSpeed = Math.min(carBeanNotRunning.maxSpeed, p.roadBean.speedLimit);
                                    carBeanQueue.add(carBeanNotRunning);
                                    break;

                                }
                            }
                        }

                        if (carBeanNotRunning.isStart) break;
                    }

                    if (carBeanNotRunning.isStart) break;

                }

            }
        }
    }


    private static int findTheVisitedEdge(int startCrossIndex, int endCrossIndex) {


        int crossId = Graph[endCrossIndex].crossId;

        for (ArcBox p = Graph[startCrossIndex].firstout; p != null; p = p.tlink) {

            if (p.roadBean.endCross == crossId) {
                return p.roadBean.roadId;
            }

        }
        throw new RuntimeException("内部数据异常!");
    }

    private static int findTheDirection(int startCrossIndex, int middleCrossIndex, int endCrossIndex) {

        int firstDirection = -1;
        int secondDirection = -1;

        for (ArcBox p = Graph[middleCrossIndex].firstin; p != null; p = p.hlink) {

            if (p.roadBean.startCross == Graph[startCrossIndex].crossId) {
                firstDirection = p.headDirection;
                break;
            }
        }

        for (ArcBox q = Graph[middleCrossIndex].firstout; q != null; q = q.tlink) {

            if (q.roadBean.endCross == Graph[endCrossIndex].crossId) {
                secondDirection = q.tailDirection;
            }
        }


        int direction = -1;

        if ((firstDirection + 1) % 4 == secondDirection) {
            direction = Constant.LEFT;
        } else {


            firstDirection = firstDirection - 1 >= 0 ? firstDirection : firstDirection + 4;
            if ((firstDirection - 1) % 4 == secondDirection) {
                direction = Constant.RIGHT;
            } else {
                direction = Constant.STRAIGHT;
            }

        }
        return direction;
    }


    private static int getTheRoadId(int startCrossIndex, int endCrossIndex) {

        int scrossId = Graph[startCrossIndex].crossId;
        int ecrossId = Graph[endCrossIndex].crossId;

        for (RoadBean roadBean : roadBeanList) {
            if (roadBean.startCross == scrossId && roadBean.endCross == ecrossId) {
                return roadBean.roadId;
            }
        }
        return 0;
    }

    //一个时间片运行完成
    private static boolean isAllNormalStopStatus() {

        for (int i = 0; i < carBeanList.size(); i++) {
            if (carBeanList.get(i).isWaiting) {

                return false;
            }
        }
        return true;
    }


}
