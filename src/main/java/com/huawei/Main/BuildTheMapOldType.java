package com.huawei.Main;

import com.huawei.beans.CarBean;
import com.huawei.beans.CrossBean;
import com.huawei.beans.RoadBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 构建图
 */
public class BuildTheMapOldType {


    public static List<CrossBean> crossBeanList = new ArrayList<>();
    public static List<RoadBean> roadBeanList = new ArrayList<>();
    public static List<CarBean> carBeanList = new ArrayList<>();
    public static int[][] path;

    static {
        readCrossBeanList();
        readRoadBeanList();
        readCarBeanList();
        Collections.sort(carBeanList);
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
                        carBeanList.add(new CarBean(carId, startCrossId, endCrossId, maxSpeed, time, false));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return carBeanList;
    }


    //邻接表   修改为十字链表
    static class VertexNode {
        int crossId;
        EdgeNode next;

        VertexNode(int crossId) {
            this.crossId = crossId;
        }
    }

    static class EdgeNode {
        int direction;
        RoadBean roadBean;
        EdgeNode next;

        EdgeNode(int direction, RoadBean roadBean, EdgeNode next) {
            this.direction = direction;
            this.roadBean = roadBean;
            this.next = next;
        }
    }


    static class ArcBox {

        //该弧的尾部所在的位置
        int tailVex; //表示数组中的索引位置
        //该弧的头部所在位置
        int headVex;

        //弧头相同的节点
        ArcBox hlink;
        //弧尾相同的节点
        ArcBox tlink;
        //此条弧的信息
        RoadBean roadBean;

    }

    static class VexNode{

        int crossId;
        //指向该顶点的入弧
        ArcBox firstin;
        //指向该顶点的出弧
        ArcBox fitstout;
    }


    public static void main(String... args) {
        System.out.println(carBeanList.size());
        VertexNode[] vertexNodes = BuildTheVertecNodesMap();
        path = new int[vertexNodes.length][vertexNodes.length];

       /* for (int i = 0; i < carBeanList.size(); i++) {
            CarBean carBean = carBeanList.get(i);
            int startBean = carBean.startBean;
            if (path[startBean][0] == 1) continue;
            ShortestPath_DJSTR(vertexNodes, startBean, path);
        }*/

        ShortestPath_DJSTR(vertexNodes, 1, path);
        System.out.println(path);
    }

    /**
     * 对于每辆车判断可能的路径
     */
  /*  private static void ArrangeTheCar(VertexNode verTexNodes[]) {
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

                            carBean.s1 += carBean.currSpeed;
                            carBean.isWaiting = false;
                        }
                    }
                }
            }


            //调度等待车辆  按照路口ID升序

            while (isAllNormalStopStatus()) {
                for (int i = 1; i < verTexNodes.length; i++) {
//                    verTexNodes[i]
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
                    //到达调度时间
                    ShortestPath_DJSTR(verTexNodes, carBeanNotRunning.startBean, path);
                    //可能到达的第一个地点
                    int dest = path[carBeanNotRunning.startBean][1];
                    for (EdgeNode p = verTexNodes[carBeanNotRunning.startBean].next; p != null; p = p.next) {
                        //找到相应的路径信息
                        if (p.roadBean.endCross == dest) {

                            LinkedList<CarBean>[] carBeanQueues = p.roadBean.carBeanQueues;
                            //道路是否允许车通过
                            for (int q = 0; q < carBeanQueues.length; q++) {

                                //道路中的每一条车道
                                LinkedList<CarBean> carBeanQueue = carBeanQueues[q];
                                //双端队列

                                //没有车  开启当前车辆
                                if (carBeanQueue.size() == 0) {
                                    carBeanNotRunning.isStart = true;
                                    carBeanNotRunning.currentRoadBean = p.roadBean;
                                    carBeanNotRunning.startBean = time;
                                    //默认开启车辆的位置为1
                                    carBeanNotRunning.s1 = p.roadBean.roadLength - 1;
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

    }*/

    //一个时间片运行完成
    private static boolean isAllNormalStopStatus() {

        for (int i = 0; i < carBeanList.size(); i++) {
            if (carBeanList.get(i).isWaiting) {

                return false;
            }
        }
        return true;
    }

    //verTexId表示某个节点的crossId  verTexI　此函数需要数组的id和 crossId对应
    public static void ShortestPath_DJSTR(VertexNode verTexNodes[], int verTexId, int path[][]) {

//        int path[][] = new int[verTexNodes.length][verTexNodes.length];
        int d[] = new int[verTexNodes.length];

        for (int i = 1; i < d.length; i++) {
            d[i] = Integer.MAX_VALUE;
            //表示最短路径没有发现
            path[i][0] = 0;
        }

        EdgeNode next = verTexNodes[verTexId].next;
        while (next != null) {

            RoadBean roadBean = next.roadBean;
            d[roadBean.endCross] = roadBean.roadLength;
            //保留路径信息
            path[roadBean.endCross][1] = verTexId;
            path[roadBean.endCross][2] = roadBean.endCross;
            //目前还未知
            path[roadBean.endCross][3] = 0;
            next = next.next;
        }

        //找出最短路径
        path[verTexId][0] = 1;
        d[verTexId] = 0;
        int k = -1;
        // verTexNodes.length-1个点
        for (int i = 2; i < verTexNodes.length; i++) {

            int min = Integer.MAX_VALUE;

            for (int j = 1; j < verTexNodes.length; j++) {
                //找出最小路径
                if (path[j][0] == 0 && d[j] < min) {
                    k = j;
                    min = d[j];
                }
            }

            if (min == Integer.MAX_VALUE) return ;

            path[k][0] = 1;
            EdgeNode p = verTexNodes[k].next;
            while (p != null) {
                RoadBean roadBean = p.roadBean;
                if (path[roadBean.endCross][0] == 0 && d[roadBean.endCross] > d[k] + roadBean.roadLength) {
                    d[roadBean.endCross] = d[k] + roadBean.roadLength;
                    int t = 1;
                    while (path[k][t] != 0) {
                        path[roadBean.endCross][t] = path[k][t];
                        t++;
                    }
                    path[roadBean.endCross][t] = roadBean.endCross;
                    path[roadBean.endCross][t + 1] = 0;
                }
                p = p.next;

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

    //构建调度地图
    private static VertexNode[] BuildTheVertecNodesMap() {
        VertexNode[] vertexNodes = new VertexNode[crossBeanList.size() + 1];

        for (int i = 1; i < vertexNodes.length; i++) {
            CrossBean crossBean = crossBeanList.get(i - 1);
            VertexNode vertexNode = new VertexNode(crossBean.crossId);
            vertexNodes[i] = vertexNode;

            EdgeNode edgeNode = null;
            RoadBean findRoadBean = null;
            if (crossBean.upRoadId != -1 && (findRoadBean = findRoadBean(crossBean.upRoadId, crossBean.crossId)) != null) {
                vertexNode.next = new EdgeNode(0, findRoadBean, null);
                edgeNode = vertexNode.next;
            }
            if (crossBean.rightRoadId != -1 && (findRoadBean = findRoadBean(crossBean.rightRoadId, crossBean.crossId)) != null) {
                if (edgeNode == null) {
                    vertexNode.next = new EdgeNode(1, findRoadBean, null);
                    edgeNode = vertexNode.next;
                } else {
                    edgeNode.next = new EdgeNode(1, findRoadBean, null);
                    edgeNode = edgeNode.next;
                }
            }


            if (crossBean.downRoadId != -1 && (findRoadBean = findRoadBean(crossBean.downRoadId, crossBean.crossId)) != null) {
                if (edgeNode == null) {
                    vertexNode.next = new EdgeNode(2, findRoadBean, null);
                    edgeNode = vertexNode.next;
                } else {
                    edgeNode.next = new EdgeNode(2, findRoadBean, null);
                    edgeNode = edgeNode.next;
                }
            }


            if (crossBean.leftRoadId != -1 && (findRoadBean = findRoadBean(crossBean.leftRoadId, crossBean.crossId)) != null) {
                if (edgeNode == null) {
                    vertexNode.next = new EdgeNode(3, findRoadBean, null);
                    edgeNode = vertexNode.next;
                } else {
                    edgeNode.next = new EdgeNode(3, findRoadBean, null);
                    edgeNode = edgeNode.next;
                }
            }

        }
        return vertexNodes;
    }


    public static RoadBean findRoadBean(int RoadId, int startCrossId) {

        for (int i = 0; i < roadBeanList.size(); i++) {
            RoadBean roadBean = roadBeanList.get(i);
            if (roadBean.roadId == RoadId && startCrossId == roadBean.startCross) {
                return roadBean;
            }
        }
        return null;
    }
}
