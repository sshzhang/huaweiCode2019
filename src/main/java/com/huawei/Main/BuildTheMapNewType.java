package com.huawei.Main;

import com.huawei.beans.CarBean;
import com.huawei.beans.CrossBean;
import com.huawei.beans.RoadBean;

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
        for (int e = 0; e < roadBeanList.size(); e++) {
            RoadBean roadBean = roadBeanList.get(e);
            int startCross = roadBean.startCross;
            int endCross = roadBean.endCross;
            int i = LocateVex(vertexNodes, startCross);
            int j = LocateVex(vertexNodes, endCross);

            ArcBox arcBox = new ArcBox(i, j, vertexNodes[j].firstin, vertexNodes[i].firstout, roadBean);
            //设置图中链头的插入
            vertexNodes[i].firstout = vertexNodes[j].firstin = arcBox;
        }
        return vertexNodes;
    }


    public static void main(String... args) {

        int[][] path = new int[Graph.length][Graph.length];
        ShortestPath_DJSTR(Graph, 0, path);
        System.out.println(path);
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

        //方向
        int tailDirection;

        int headDirection;

        //弧头相同的节点
        ArcBox hlink;
        //弧尾相同的节点
        ArcBox tlink;
        //此条弧的信息
        RoadBean roadBean;


        ArcBox(int tailVex, int headVex, ArcBox hlink, ArcBox tlink, RoadBean roadBean) {

            this.tailVex = tailVex;
            this.headVex = headVex;
            this.hlink = hlink;
            this.tlink = tlink;
            this.roadBean = roadBean;
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

        return;

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

        while (isAllFinish()) {//依然有车量在运行

            time++;

            //扫描道路中的每一条车道
            for (int i = 0; i < roadBeanList.size(); i++) {//总共的道路数
                //扫描所有的车道
                RoadBean roadBean = roadBeanList.get(i);
                LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;


                //运行车辆并标注车的状态
                for (LinkedList<CarBean> carBeans : carBeanQueues) {
                    //每一个车道中所有车辆信息
                    for (int index = carBeans.size() - 1; index >= 0; index--) {

                        CarBean carBean = carBeans.get(index);
                        //carBean 后一辆车
                        CarBean preBean = index == carBeans.size() - 1 ? null : carBeans.get(index + 1);

                        //之前车辆的速度
                        int preCarMaxSpeed = index == carBeans.size() - 1 ? roadBean.speedLimit : preBean.currSpeed;
                        //求出当前车的速度
                        carBean.currSpeed = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), preCarMaxSpeed);
                        //当前车距离上一辆车的距离    如果当前车没有上一辆车  直接是到尽头的距离
                        int distance = index == carBeans.size() - 1 ? carBean.s1 : carBean.s1 - preBean.s1 - 1;

                        if (carBean.currSpeed > distance) {//等待转弯调度
                            if (index == carBeans.size() - 1) {//当前车是最前车
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
                        }
                    }
                }
            }


            //调度等待车辆  按照路口ID升序

            while (isAllNormalStopStatus()) {
                for (int i = 1; i < verTexNodes.length; i++) {

                    //安装道路id升序调度
                    for (ArcBox p = verTexNodes[i].firstin; p != null; p = p.hlink) {
                        //道路的出口点
                        RoadBean roadBean = p.roadBean;
                        LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
                        int carBeanQueuesLength = carBeanQueues.length;

                        //TODO
                    }
                }
            }

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
                    ShortestPath_DJSTR(verTexNodes, LocateVex(verTexNodes,carBeanNotRunning.startBean), path);
                    //可能到达的第一个地点
                    int dest = path[carBeanNotRunning.endBean][1];
                    for (ArcBox p = verTexNodes[LocateVex(verTexNodes,carBeanNotRunning.startBean)].firstout; p != null; p = p.tlink) {
                        //找到相应的路径信息
                        if (p.roadBean.endCross == dest) {

                            LinkedList<CarBean>[] carBeanQueues = p.roadBean.carBeanQueues;
                            //道路是否允许车通过
                            for (int q = 0; q < carBeanQueues.length; q++) {

                                //道路中的每一条车道
                                LinkedList<CarBean> carBeanQueue = carBeanQueues[q];
                                //双端队列

                                //没有车  开启当前车辆
                                if (carBeanQueue==null||carBeanQueue.size() == 0) {
                                    carBeanNotRunning.isStart = true;
                                    carBeanNotRunning.currentRoadBean = p.roadBean;
                                    carBeanNotRunning.startBean = time;
                                    //默认开启车辆的位置为1
                                    carBeanNotRunning.s1 = p.roadBean.roadLength - 1;

                                    //设置访问路径  其中的值是交叉路口对应的数组位置
                                    int t = 0;
                                    while (path[carBeanNotRunning.endBean][t] != -1) {
                                        carBeanNotRunning.preVisted[t] = path[carBeanNotRunning.endBean][t];
                                    }
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

                                        int t = 0;
                                        while (path[carBeanNotRunning.endBean][t] != -1) {
                                            carBeanNotRunning.preVisted[t] = path[carBeanNotRunning.endBean][t];
                                        }

                                        //当前车速 最小值  没有用到
                                        carBeanNotRunning.currSpeed = Math.min(carBeanNotRunning.maxSpeed, p.roadBean.speedLimit);
                                        carBeanQueue.add(carBeanNotRunning);
                                        break;

                                    }
                                }
                            }

                            if (carBeanNotRunning.isStart) break;
                        }

                    }

                }
            }


        }

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
