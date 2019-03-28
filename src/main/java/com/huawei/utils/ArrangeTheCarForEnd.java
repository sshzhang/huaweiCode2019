
package com.huawei.utils;

import com.huawei.beans.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 车辆调度类
 */
public class ArrangeTheCarForEnd {


    private static int count = 0;
    private static VexNode[] Graph = null;

    private static List<CrossBean> crossBeanList = new ArrayList<>();
    private static List<RoadBean> roadBeanList = new ArrayList<>();
    private static List<CarBean> carBeanList = new ArrayList<>();

    public static void ArrangeTheCar(List<CrossBean> crossBeanList, List<RoadBean> roadBeanList, List<CarBean> carBeanList, String answerPath) {


        ArrangeTheCarForEnd.crossBeanList = crossBeanList;
        ArrangeTheCarForEnd.roadBeanList = roadBeanList;
        ArrangeTheCarForEnd.carBeanList = carBeanList;
        BuildTheVertecNodesMap();
        int time = 0;

        //Date date1 = new Date();
        //System.out.println(date1.getHours() + " " + date1.getMinutes());
        //开始时间22:27
        while (!isAllFinish()) {
            System.out.println(time);
            //System.out.println("FlageTheWaitingCar");
            FlageTheWaitingCar();
            //System.out.println("RunTheWaitingCar");
            RunTheWaitingCar();
            //运行车库中车辆
            //System.out.println("driverAllCarInGarage");
            driverAllCarInGarage(time);
            time++;
        }
        BufferedWriter bufferedWriter = null;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(answerPath));
            for (int i = 0; i < ArrangeTheCarForEnd.carBeanList.size(); i++) {
                CarBean carBean = ArrangeTheCarForEnd.carBeanList.get(i);
                StringBuilder builder = new StringBuilder();
                builder.append("(" + carBean.carId + ", " + carBean.startTime);
                for (int index = 0; index < carBean.index; index++) {
                    builder.append(", " + carBean.visitedEdge[index]);
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

        //Date date = new Date(System.currentTimeMillis());
        //System.out.println(date.getHours() + " " + date.getMinutes());
    }


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


    //开启车库中的车
    private static void driverAllCarInGarage(int time) {

        //启动每一个未开启的车辆
        for (int k = 0; k < carBeanList.size(); k++) {
            CarBean carBeanr = carBeanList.get(k);
            //车没有调度完成  限制当前最多存在500辆车

            if (count >= 980) break;

            if (!carBeanr.isFinish && !carBeanr.isStart) {

                //调度没有运行的车辆
                CarBean carBeanNotRunning = carBeanList.get(k);
                //没有到调度时间
                if (carBeanNotRunning.startTime > time - 1) continue;

                //重新计算边的情况
                CheckThePreparingStartingRoad(carBeanNotRunning);
                int[][] path = new int[Graph.length][Graph.length];
                int[] visited = new int[Graph.length];
                //到达调度时间
                ShortestPath_DJSTR(-1, LocateVex(carBeanNotRunning.startBean), path, visited);

                //可能到达的第一个地点
                //可达
                if (visited[LocateVex(carBeanNotRunning.endBean)] == 1) {

                    //path[LocateVex(carBeanNotRunning.endBean)][1]

                    for (ArcBox q = Graph[LocateVex(carBeanNotRunning.startBean)].firstout; q != null; q = q.tlink) {

                        if (LocateVex(q.roadBean.endCross) == path[LocateVex(carBeanNotRunning.endBean)][1]) {

                            RoadBean roadBean = q.roadBean;

                            LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;

                            boolean iset = false;

                            for (int i = 0; i < carBeanQueues.length; i++) {
                                if (carBeanQueues[i].size() == 0) {
                                    count++;
                                    carBeanNotRunning.isStart = true;
                                    carBeanNotRunning.currentRoadBean = q.roadBean;
                                    carBeanNotRunning.visitedEdge[carBeanNotRunning.index++] = q.roadBean.roadId;
                                    carBeanNotRunning.startTime = time - 1;
                                    carBeanNotRunning.currSpeed = Math.min(carBeanNotRunning.maxSpeed, roadBean.speedLimit);
                                    carBeanNotRunning.s1 = roadBean.roadLength - carBeanNotRunning.currSpeed;
                                    carBeanQueues[i].add(carBeanNotRunning);
                                    iset = true;
                                    break;
                                } else {
                                    CarBean carBean = carBeanQueues[i].peekLast();
                                    if (carBean.s1 >= roadBean.roadLength - 1) {//当前车道堵住
                                        continue;
                                    } else {
                                        count++;
                                        carBeanNotRunning.isStart = true;
                                        carBeanNotRunning.currentRoadBean = q.roadBean;
                                        carBeanNotRunning.currSpeed = Math.min(Math.min(carBeanNotRunning.maxSpeed, roadBean.speedLimit), roadBean.roadLength - carBean.s1 - 1);
                                        carBeanNotRunning.isWaiting = false;
                                        carBeanNotRunning.visitedEdge[carBeanNotRunning.index++] = q.roadBean.roadId;
                                        carBeanNotRunning.startTime = time - 1;
                                        //设置距离
                                        carBeanNotRunning.s1 = roadBean.roadLength - carBeanNotRunning.currSpeed;
                                        carBeanQueues[i].add(carBeanNotRunning);
                                        iset = true;
                                        break;
                                    }
                                }
                            }

                            if (!iset && carBeanNotRunning.isStart) throw new RuntimeException("数据格式异常");

                        }
                    }

                }

            }
        }
    }


    private static void RunTheWaitingCar() {
        //调度等待车辆
        while (!isAllNormalStopStatus()) {
            //按照路口id降序 扫描所有路口
            for (int i = 0; i < Graph.length; i++) {
                //路口对应的出口道路 按照道路id降序扫描
                for (ArcBox p = Graph[i].firstin; p != null; p = p.hlink) {
                    RoadBean roadBean = p.roadBean;
                    LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
                    int index = 0;
                    while (index < roadBean.roadNums) {  //搜索所有车道

                        LinkedList<CarBean> carBeanQueue = carBeanQueues[index];

                        if (carBeanQueue.size() == 0) {
                            index++;
                            continue;
                        }
                        CarBean peek = carBeanQueue.peek();
                        //如果没有在等待,调整下一个车道
                        if (!peek.isWaiting) {//下一个车道调度
                            index++;
                            continue;
                        }else{//等待检验
                            index = 0;
                        }
                        /**
                         * 运行等待的车辆分为三步
                         *    1.已经设置方向就不存在设置方向这一说
                         *        a)已经设置过方向 按照当前方向调用
                         *        b)没有设置过方向 按照当前道路情况设置方向,
                         *                            b.1)如果设置的方向不可通行，  按照没有考虑实际情况的最短路径规划方向
                         *                            b.2)如果设置的方向可行, 继续处理
                         */

                        //如果当前车已经设置方向,就不存在设置方向这一说,直接按照之前规划的方向运行
                        if (peek.isSetDirection) {//设置过方向, 说明车辆当前不可能使向最终的目的地
                            //当前车需要走的下一个道路id
                            int roadId = peek.visitedEdge[peek.index - 1];
                            //当前车辆所在道路的结束路口
                            int endCross = roadBean.endCross;
                            RoadBean nextRoadBean = null;
                            for (ArcBox q = Graph[LocateVex(endCross)].firstout; q != null; q = q.tlink) {
                                if (q.roadBean.roadId == roadId) {
                                    nextRoadBean = q.roadBean;
                                    break;
                                }
                            }

                            if (nextRoadBean == null) throw new RuntimeException("内部数据格式异常");
                            //直行、左转、右转
                            //不存在到达终点的情况
                            if (peek.endBean == roadBean.endCross) throw new RuntimeException("数据格式异常");


                            //判断当前车的转弯方向
                            //roadBean.startCross---->roadBean.endCross---->nextRoadBean.endCross;

                            int direction = FindTheDirection(LocateVex(roadBean.startCross), LocateVex(roadBean.endCross), LocateVex(nextRoadBean.endCross));

                            //直行
                            if (direction == Constant.STRAIGHT) {

                                if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean)) {
                                    //++;//能改变方向,可以调度下一个车道,否则直接到下一个道路

                                    //index++;
                                    //index = (index + 1) % roadBean.roadLength;
                                    index = (index + 1) % roadBean.roadNums;
                                    continue;
                                } else {
                                    break;
                                }

                            } else if (direction == Constant.LEFT) {

                                //直行道路
                                RoadBean strRoadBean = null;
                                int newDirection = p.headDirection - 1 < 0 ? (4 + p.headDirection - 1) % 4 : (p.headDirection - 1) % 4;
                                for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                                    if (q.headDirection == newDirection) { //找出直行对应的道路
                                        strRoadBean = q.roadBean;
                                        break;
                                    }
                                }

                                if (strRoadBean != null && BlockingFlages(strRoadBean, Constant.STRAIGHT)) {
                                    //index++;//继续下一个车道
                                    break;//调度不成功  直接调度下一个道路
                                }

                                if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean)) {
                                    //index = (index + 1) % roadBean.roadLength;
                                    //index++;

                                    index = (index + 1) % roadBean.roadNums;
                                    continue;
                                } else {
                                    break;
                                }
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
                                //找出直行对应的道路
                                if (strRoadBean != null && BlockingFlages(strRoadBean, Constant.STRAIGHT)) {
                                    //index++;
                                    //continue;
                                    break;//调度不成功  直接调度下一个道路
                                }
                                //找出左转对应的道路
                                if (leftRoadBean != null && BlockingFlages(leftRoadBean, Constant.LEFT)) {
                                    //index++;
                                    //continue;
                                    break;//调度不成功  直接调度下一个道路
                                }
                                if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean)) {
                                    // index++;
                                    //index = (index + 1) % roadBean.roadLength;
                                    index = (index + 1) % roadBean.roadNums;
                                    continue;
                                } else {
                                    break;
                                }
                            } else {
                                throw new RuntimeException("方向数据异常");
                            }

                        } else {//如果当前车没有设置方向
                            //如果当前车到达目的地
                            if (peek.endBean == roadBean.endCross) {
                                //判断是跳转到下一个车道还是其它道路
                                boolean isbreak = false;
                                //判断速度是否满足情况
                                int speed = Math.min(peek.maxSpeed, roadBean.speedLimit);
                                if (speed >= peek.s1) {//当前能进入
                                    //完成
                                    peek.isWaiting = false;
                                    peek.isFinish = true;
                                    count--;
                                    carBeanQueue.poll();
                                    peek.visitedEdge[peek.index] = -1;
                                    //调整此条车道后续的车辆
                                    for (int k = 0; k < carBeanQueue.size(); k++) {
                                        CarBean carBean = carBeanQueue.get(k);
                                        //当前是第一辆且最终的目的地就是此地
                                        if (k == 0 && carBean.endBean == roadBean.endCross) {
                                            speed = Math.min(carBean.maxSpeed, roadBean.speedLimit);
                                            if (speed >= carBean.s1) {
                                                carBean.isWaiting = false;
                                                carBean.isFinish = true;
                                                count--;
                                                //保存结束状态-1
                                                carBean.visitedEdge[carBean.index] = -1;
                                                carBeanQueue.poll();
                                                k--;
                                            } else {//不可到达
                                                carBean.isWaiting = false;
                                                carBean.s1 -= speed;
                                                ChangeWaitingToStop(roadBean, carBeanQueue);
                                                //index++;
                                                break;
                                            }
                                        } else {
                                            if (k == 0) {//没有到目的点
                                                speed = Math.min(carBean.maxSpeed, roadBean.speedLimit);
                                                if (speed <= carBean.s1) {
                                                    carBean.isWaiting = false;
                                                    carBean.s1 -= speed;
                                                    ChangeWaitingToStop(roadBean, carBeanQueue);
                                                }
                                                break;
                                            } else {//不存在
                                                throw new RuntimeException("逻辑错误");
                                            }
                                        }
                                    }
                                } else {//可能进入
                                    isbreak = true;
                                    peek.isWaiting = false;
                                    peek.s1 -= speed;
                                    ChangeWaitingToStop(roadBean, carBeanQueue);
                                }
                                if (isbreak) {
                                    break;
                                } else {
                                    //index++;
                                    index = (index + 1) % roadBean.roadNums;
                                    //index = (index + 1) % roadBean.roadLength;
                                    continue;
                                }
                            } else {//当前车不是目的地  没设置过方向
                                //TODO 尽量避免死锁
                                /**
                                 * 策略
                                 * 先判断当前方向下 车能不能通过路口
                                 * 如果不能通过路口
                                 *        1)  如果当前方向等待的次数超过5次, 直接设置当前方向不可通行，另选择其它方向
                                 *        2)没有超过固定次数继续等待轮训 ,如果方向不一致需要重置计数功能
                                 * 如果能够通过入口
                                 *         //设置方向  准备调度
                                 */
                                int startCrossIndex = -1;
                                int nstartCrossIndex = -1;
                                int endCrossIndex = -1;
                                //设置方向
                                CheckTheRoadBusiness(peek);
                                int[][] path = new int[Graph.length][Graph.length];
                                int[] visited = new int[Graph.length];
                                //LocateVex(peek.endBean) 表示从peek.currentRoadBean.endCross==roadBean.endCross到其它节点的距离
                                ShortestPath_DJSTR(LocateVex(roadBean.startCross), LocateVex(roadBean.endCross), path, visited);
                                //不存在最短路径
                                if (visited[LocateVex(peek.endBean)] == 0) {
                                    path = new int[Graph.length][Graph.length];
                                    visited = new int[Graph.length];
                                    //System.out.println("产生异常数据!");
                                    ShortestPath_DJSTRAbSolute(LocateVex(roadBean.startCross), LocateVex(roadBean.endCross), path, visited);
                                    if (visited[LocateVex(peek.endBean)] == 0) throw new RuntimeException("数据格式异常");
                                    startCrossIndex = LocateVex(roadBean.startCross);
                                    nstartCrossIndex = LocateVex(roadBean.endCross);
                                    endCrossIndex = path[LocateVex(peek.endBean)][1];


                                } else {//存在最短路径
                                    startCrossIndex = LocateVex(roadBean.startCross);
                                    nstartCrossIndex = LocateVex(roadBean.endCross);
                                    endCrossIndex = path[LocateVex(peek.endBean)][1];
                                }
                                if (startCrossIndex == -1 || nstartCrossIndex == -1 || endCrossIndex == -1)
                                    throw new RuntimeException("数据异常");
                                //首先判断是否能转入道路
                                //roadBean   endCrossIndex
                                RoadBean nextRoad = null;
                                for (ArcBox k = Graph[nstartCrossIndex].firstout; k != null; k = k.tlink) {
                                    if (LocateVex(k.roadBean.endCross) == endCrossIndex) {
                                        nextRoad = k.roadBean;
                                        break;
                                    }
                                }
                                if (nextRoad == null) throw new RuntimeException("数据异常");

                                //TODO peek当前车量   roadBean当前车辆所在的道路  nextRoad当前车辆要转向到的道路

                                // TODO 目前判断当前车存在允许通行的路径

                                //设置方向为true
                                peek.isSetDirection = true;
                                peek.visitedEdge[peek.index++] = nextRoad.roadId;
                                //判断转向及调度
                                int direction = FindTheDirection(startCrossIndex, nstartCrossIndex, endCrossIndex);
                                //直行
                                if (direction == Constant.STRAIGHT) {

                                    if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoad)) {
                                        //index++;//能改变方向,可以调度下一个车道,否则直接到下一个道路
                                       // index = (index + 1) % roadBean.roadLength;
                                        index = (index + 1) % roadBean.roadNums;
                                        continue;
                                    } else {
                                        break;
                                    }

                                } else if (direction == Constant.LEFT) {
                                    //直行道路
                                    RoadBean strRoadBean = null;
                                    int newDirection = p.headDirection - 1 < 0 ? (4 + p.headDirection - 1) % 4 : (p.headDirection - 1) % 4;
                                    for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                                        if (q.headDirection == newDirection) { //找出直行对应的道路
                                            strRoadBean = q.roadBean;
                                            break;
                                        }
                                    }

                                    if (strRoadBean != null && BlockingFlages(strRoadBean, Constant.STRAIGHT)) {
                                        //index++;//继续下一个车道
                                        break;//调度不成功  直接调度下一个道路
                                    }

                                    if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoad)) {
                                        //index++;
                                        //index = (index + 1) % roadBean.roadLength;
                                        index = (index + 1) % roadBean.roadNums;
                                        continue;
                                    } else {
                                        break;
                                    }


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
                                    //找出直行对应的道路
                                    if (strRoadBean != null && BlockingFlages(strRoadBean, Constant.STRAIGHT)) {
                                        //index++;
                                        //continue;
                                        break;//调度不成功  直接调度下一个道路
                                    }
                                    //找出左转对应的道路
                                    if (leftRoadBean != null && BlockingFlages(leftRoadBean, Constant.LEFT)) {
                                        //index++;
                                        //continue;
                                        break;//调度不成功  直接调度下一个道路
                                    }
                                    if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoad)) {

                                        //index++;
                                        //index = (index + 1) % roadBean.roadLength;
                                        index = (index + 1) % roadBean.roadNums;
                                        continue;
                                    } else {
                                        break;
                                    }

                                } else {
                                    throw new RuntimeException("方向数据异常");
                                }

                            }
                        }

                    }
                }
            }
        }
    }

    private static void ChangeWaitingToStop(RoadBean roadBean, LinkedList<CarBean> carBeanQueue) {
        int speed;
        for (int k = 1; k < carBeanQueue.size(); k++) {

            CarBean carBean = carBeanQueue.get(k);
            if (!carBean.isWaiting) break;

            carBean.isWaiting = false;

            CarBean preCarBean = carBeanQueue.get(k - 1);
            speed = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), carBean.s1 - preCarBean.s1 - 1);
            carBean.s1 -= speed;
        }
    }


    /**
     * 判断车辆是否堵住
     *
     * @param strRoadBean 表示当前判断的道路
     * @param direction   当前道路是否存在direction方向上的等待转弯车辆
     * @return false 表示不存在堵住的情况    true表示存在堵住
     */

    private static boolean BlockingFlages(RoadBean strRoadBean, int direction) {

        LinkedList<CarBean>[] carBeanQueues = strRoadBean.carBeanQueues;

        for (int i = strRoadBean.index; i < carBeanQueues.length; i++) {

            LinkedList<CarBean> carBeanQueue = carBeanQueues[i];
            if (carBeanQueue.size() == 0) {//为0
                continue;
            } else {//车道上的数据不为空
                CarBean peek = carBeanQueue.peek();
                if (!peek.isWaiting) continue;

                /**
                 * 两种情况当前车已经设置过方向 直接取
                 * 没设置过方向， 先设置 再判断
                 */
                //当前这辆车到达了目的地 不会转方向
                if (strRoadBean.endCross == peek.endBean) return false;


                if (!peek.isSetDirection) {//没有设置过方向
                    CheckTheRoadBusiness(peek);
                    int[][] path = new int[Graph.length][Graph.length];
                    int[] visited = new int[Graph.length];
                    ShortestPath_DJSTR(LocateVex(strRoadBean.startCross), LocateVex(strRoadBean.endCross), path, visited);

                    if (visited[LocateVex(peek.endBean)] == 0) {//不存在最短距离
                        //找出绝对最短距离
                        path = new int[Graph.length][Graph.length];
                        visited = new int[Graph.length];
                        ShortestPath_DJSTRAbSolute(LocateVex(strRoadBean.startCross), LocateVex(strRoadBean.endCross), path, visited);

                        if (visited[LocateVex(peek.endBean)] == 0) throw new RuntimeException("方向数据异常");

                        else {//设置方向
                            peek.isSetDirection = true;
                            //设置即将走的对应道路
                            //strRoadBean.endCross --->path[LocateVex(peek.endBean)][1];
                            RoadBean roadcurr = null;
                            for (ArcBox k = Graph[LocateVex(strRoadBean.endCross)].firstout; k != null; k = k.tlink) {
                                if (LocateVex(k.roadBean.endCross) == path[LocateVex(peek.endBean)][1]) {
                                    roadcurr = k.roadBean;
                                    break;
                                }
                            }

                            if (roadcurr == null) throw new RuntimeException("数据格式异常!");
                            peek.visitedEdge[peek.index++] = roadcurr.roadId;


                            int nendCrossIndex = LocateVex(roadcurr.endCross);
                            //判断是否干扰

                            return isInTheSameDirection(LocateVex(strRoadBean.startCross), LocateVex(strRoadBean.endCross), nendCrossIndex, direction);

                        }
                    } else {//存在最短距离

                        peek.isSetDirection = true;

                        int nendCrossIndex = path[LocateVex(peek.endBean)][1];
                        //找出这条边

                        RoadBean roadBean = null;
                        for (ArcBox k = Graph[LocateVex(strRoadBean.endCross)].firstout; k != null; k = k.tlink) {

                            if (nendCrossIndex == LocateVex(k.roadBean.endCross)) {
                                roadBean = k.roadBean;
                                break;
                            }
                        }
                        if (roadBean == null) throw new RuntimeException("数据格式异常");

                        peek.visitedEdge[peek.index++] = roadBean.roadId;

                        return isInTheSameDirection(LocateVex(strRoadBean.startCross), LocateVex(strRoadBean.endCross), LocateVex(roadBean.endCross), direction);
                    }
                } else {//直接取方向

                    int roadId = peek.visitedEdge[peek.index - 1];
                    int endCrossIndex = LocateVex(strRoadBean.endCross);
                    //找出道路roadId对应的起点为endCross 终点
                    int nendCrossIndex = -1;
                    for (ArcBox k = Graph[endCrossIndex].firstout; k != null; k = k.tlink) {

                        if (k.roadBean.roadId == roadId) {
                            nendCrossIndex = k.roadBean.endCross;
                            break;
                        }
                    }
                    if (nendCrossIndex == -1) throw new RuntimeException("数据格式异常");
                    nendCrossIndex = LocateVex(nendCrossIndex);

                    return isInTheSameDirection(LocateVex(strRoadBean.startCross), endCrossIndex, nendCrossIndex, direction);
                }
            }
        }
        //道路车辆为空或者所有车都是终止状态
        return false;
    }

    private static boolean isInTheSameDirection(int startCrossIndex, int nstartCrossIndex, int endCrossIndex, int direction) {


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

    }


    /**
     * 调度当前处于等待状态的车辆 carBean  到道路RoadBean上
     *
     * @param
     * @param roadBean
     * @param carBeanQueue
     * @param carBean
     * @param nextRoad
     * @return
     */

    private static boolean waitingCarChangeDirection(RoadBean roadBean, LinkedList<CarBean> carBeanQueue, CarBean carBean, RoadBean nextRoad) {

        //System.out.println(carBean.carId + "   " + carBean.startBean + "    " + carBean.endBean + " " + carBean.currentRoadBean.roadId + "  ");
        //判断即将转到的新道路情况
        LinkedList<CarBean>[] carBeanQueuesnext = nextRoad.carBeanQueues;
        //判断是否调度成功
        boolean isSuccess = false;
        //v2可能的速度
        int v2 = Math.min(carBean.maxSpeed, nextRoad.speedLimit);
        int s1 = carBean.s1;
        //等待初始化
        int s2 = -1;
        //用来标志是否设置过当前等待车辆的状态  如果没设置表明当前车只能等待
        boolean isFlages = false;
        //即将转弯到的新的道路情况,每一条车道判断
        for (int j = 0; j < carBeanQueuesnext.length; j++) {
            //每一条车道最晚进入的车辆
            CarBean carBeanNext = carBeanQueuesnext[j].peekLast();
            if (carBeanNext == null) {//此队列为空
                //调度成功  更新相应数据
                //存在速度导致调度不成功
                if (v2 - s1 <= 0) {//说明不能通过入口
                    carBean.s1 = 0;//当前车只能到达当前路口最前面位置
                    //更新当前车道上所有等待车辆信息
                    //为什么从k=0 开始呢 因为k=0标识队列的头  我们需要先更新最早抵达的车辆
                    StopTheMougeRoad(roadBean, carBeanQueue, carBean);

                } else {
                    carBean.isWaiting = false;
                    //查找当前道路的下一条车道
                    isSuccess = true;
                    carBean.currentRoadBean = nextRoad;
                    //如果已经设置过方向
                    if (carBean.isSetDirection) {//不需要再重新设置
                    } else {
                        carBean.visitedEdge[carBean.index++] = nextRoad.roadId;
                        throw new RuntimeException("数据格式异常");
                    }
                    carBean.isSetDirection = false;
                    //s2最大运行的距离
                    int maxS2 = v2 - s1;
                    //默认只能通过一个道路 数据为正
                    carBean.s1 = nextRoad.roadLength - maxS2;
                    carBean.currSpeed = v2;
                    //从原来队列中删除元素
                    carBeanQueue.poll();
                    //保存元素到最新队列
                    carBeanQueuesnext[j].add(carBean);
                }
                //不需要再调度
                isFlages = true;
                break;
            }

            //没有任何实际的作用,只是为了检测异常逻辑
            if (carBeanNext.s1 > nextRoad.roadLength - 1) throw new RuntimeException("车辆数据长度异常");

            //当前车辆的转弯方向有车在等待
            if (carBeanNext.isWaiting) {

                //判断速度是否能通过
                if (v2 - carBean.s1 <= 0) {//不能通过
                    carBean.s1 = 0;
                    StopTheMougeRoad(roadBean, carBeanQueue, carBean);

                } else {//可能会通过
                    //TODO update 判断速度是否大于 距离    我把  v2 修改为 nextRoad.roadLength  其它没变  我觉得这里肯定有问题
                    s2 = nextRoad.roadLength - carBeanNext.s1 - 1;
                    //不能通过 由于当前转入的方向存在一个等待状态的车辆
                    if (v2 - carBean.s1 > s2) {//说明受前方道路的影响,出现等待状态
                        //carBean.isWaiting = true;
                        //StopTheMougeRoad(roadBean, carBeanQueue, carBean);
                        //此处存在逻辑错误, 本来就不能通过， 这条路也就不用判断
                    } else {//可以转入

                        carBean.isWaiting = false;
                        //查找当前道路的下一条车道
                        isSuccess = true;

                        //如果已经设置过方向  不存在没有设置方向一说
                        if (carBean.isSetDirection) {

                        } else {
                            carBean.visitedEdge[carBean.index++] = nextRoad.roadId;
                            throw new RuntimeException("数据格式异常");
                        }

                        carBean.isSetDirection = false;
                        carBean.currentRoadBean = nextRoad;
                        //carBean.visitedEdge[carBean.index++] = nextRoad.roadId;
                        //s2最大运行的距离
                        int maxS2 = v2 - s1;
                        //默认只能通过一个道路 数据为正
                        carBean.s1 = nextRoad.roadLength - maxS2;
                        carBean.currSpeed = v2;
                        //从原来队列中删除元素
                        carBeanQueue.poll();
                        //保存元素到最新队列
                        carBeanQueuesnext[j].add(carBean);
                    }
                }

                isFlages = true;
                //直接终止,不用再判断其它车道
                break;
            } else {//当前车是终止状态
                //当前车道不能通行
                if (carBeanNext.s1 == nextRoad.roadLength - 1) {
                    //设置 isFlages来表示当前是否不能通行 isFlage=false表示不能通行
                    continue;
                } else {
                    //速度到底能不能通过 此刻v2需要变化
                    v2 = Math.min(v2, nextRoad.roadLength - carBeanNext.s1 - 1 + carBean.s1);
                    if (v2 - carBean.s1 <= 0) {//不允许通过
                        carBean.s1 = 0;
                        StopTheMougeRoad(roadBean, carBeanQueue, carBean);

                    } else {//允许通过
                        carBean.isWaiting = false;
                        //查找当前道路的下一条车道
                        isSuccess = true;
                        carBean.currentRoadBean = nextRoad;

                        //如果已经设置过方向
                        if (carBean.isSetDirection) {//不需要再重新设置

                        } else {
                            carBean.visitedEdge[carBean.index++] = nextRoad.roadId;

                            throw new RuntimeException("数据格式异常");

                        }
                        carBean.isSetDirection = false;

                        //s2最大运行的距离
                        int maxS2 = v2 - s1;
                        //默认只能通过一个道路 数据为正
                        carBean.s1 = nextRoad.roadLength - maxS2;
                        carBean.currSpeed = v2;
                        //carBean.isSetDirection = false;
                        //从原来队列中删除元素
                        carBeanQueue.poll();
                        //保存元素到最新队列
                        carBeanQueuesnext[j].add(carBean);
                    }
                    isFlages = true;
                    break;
                }
            }

        }
        //当前已经转弯 更新这条车道上的所有车的信息
        if (isSuccess) {//调度给定道路的某条车道
            //调度当前车道  只调度不出路口的车辆
            for (int k = 0; k <= carBeanQueue.size() - 1; k++) {
                CarBean carBeanCurr = carBeanQueue.get(k);
                CarBean precarBeanCurr = k != 0 ? carBeanQueue.get(k - 1) : null;
                if (!carBeanCurr.isWaiting) break;
                int currSpeed = Math.min(roadBean.speedLimit, carBeanCurr.maxSpeed);
                //不需要调整 存在出入口

                if (precarBeanCurr == null && currSpeed >= carBeanCurr.s1) {

                    //当前点到达目的地 结束状态
                    if (carBeanCurr.endBean == roadBean.endCross) {
                        carBeanCurr.isWaiting = false;
                        carBeanCurr.isFinish = true;
                        carBeanCurr.isSetDirection = false;
                        carBeanCurr.visitedEdge[carBeanCurr.index] = -1;
                        count--;
                        carBeanCurr.s1 = 0;
                        carBeanQueue.poll();
                        k--;
                        continue;
                    } else if (currSpeed > carBeanCurr.s1) {
                        //存在转弯的点 直接结束
                        break;
                    }
                    //相等按照下面处理
                }

                int distance = precarBeanCurr == null ? carBeanCurr.s1 : carBeanCurr.s1 - precarBeanCurr.s1 - 1;
                currSpeed = Math.min(currSpeed, distance);
                carBeanCurr.currSpeed = currSpeed;
                carBeanCurr.s1 = carBeanCurr.s1 - currSpeed;
                carBeanCurr.isWaiting = false;
            }
        }

        if (!isFlages) {//调度到最后停止当前车
            StopTheMougeRoad(roadBean, carBeanQueue, carBean);
        }
        return isSuccess;
    }


    /**
     * 停止某条车道上的所有车辆
     *
     * @param roadBean
     * @param carBeanQueue
     * @param carBean
     */

    private static void StopTheMougeRoad(RoadBean roadBean, LinkedList<CarBean> carBeanQueue, CarBean carBean) {
        carBean.isWaiting = false;
        for (int k = 1; k <= carBeanQueue.size() - 1; k++) {
            CarBean carBeanChange = carBeanQueue.get(k);
            if (!carBeanChange.isWaiting) {//车没有等待
                break;
            }
            CarBean precarBeanChage = carBeanQueue.get(k - 1);
            int distance = carBeanChange.s1 - precarBeanChage.s1 - 1;
            //当前速度道路限速和终止状态的距离
            int maxValue = Math.min(Math.min(roadBean.speedLimit, carBeanChange.maxSpeed), distance);
            carBeanChange.s1 = carBeanChange.s1 - maxValue;
            carBeanChange.isWaiting = false;
        }
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
     * 计算相对最短路径
     *
     * @param verTexId 表示交叉路口的索引id  待计算verTexId 到其它所有点的最短距离
     * @param path     路径数据
     * @param visited  是否存在最短路径
     */

    public static void ShortestPath_DJSTR(int preVerTexId, int verTexId, int path[][], int[] visited) {


        //设置 preVerTexId--->verTexId 为空


        //设置当前道路的反向道路不可用 不允许调头
        if (preVerTexId != -1)
            for (ArcBox q = Graph[verTexId].firstout; q != null; q = q.tlink) {
                if (LocateVex(q.roadBean.endCross) == preVerTexId) {
                    q.roadBean.visible = false;
                    break;
                }
            }


        int d[] = new int[Graph.length];

        //int[] visited = new int[Graph.length];

        for (int i = 0; i < d.length; i++) {
            d[i] = Integer.MAX_VALUE;
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
        for (int i = 1; i < Graph.length; i++) {

            int min = Integer.MAX_VALUE;

            for (int j = 0; j < Graph.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j] < min) {
                    k = j;
                    min = d[j];
                }
            }

            if (min == Integer.MAX_VALUE) return;

            visited[k] = 1;
            ArcBox p = Graph[k].firstout;
            while (p != null) {//&& (preRoadBean == null || preRoadBean.roadId != p.roadBean.roadId)
                RoadBean roadBean = p.roadBean;

                if (!roadBean.visible) {
                    p = p.tlink;
                    continue;
                }


/*  if (roadBean.isBothWay == 1) {
                    preRoadBean = FindThePositiveRoad(roadBean);
                } else {
                    preRoadBean = null;
                }*/


                int loc = LocateVex(roadBean.endCross);
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


    //计算绝对的最短距离 忽视道路上车辆的拥堵情况
    public static void ShortestPath_DJSTRAbSolute(int preVerTexId, int verTexId, int path[][], int[] visited) {

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

        int d[] = new int[Graph.length];

        //int[] visited = new int[Graph.length];

        for (int i = 0; i < d.length; i++) {
            d[i] = Integer.MAX_VALUE;
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
            d[loc] = roadBean.roadLength;
            //d[loc] = Graph[loc].crossBusiness;
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

            int min = Integer.MAX_VALUE;

            for (int j = 0; j < Graph.length; j++) {
                //找出最小路径
                if (visited[j] == 0 && d[j] < min) {
                    k = j;
                    min = d[j];
                }
            }

            if (min == Integer.MAX_VALUE) return;

            visited[k] = 1;
            ArcBox p = Graph[k].firstout;
            while (p != null) {//&& (preRoadBean == null || preRoadBean.roadId != p.roadBean.roadId)
                RoadBean roadBean = p.roadBean;
                if (novisibleRoadId != -1 && LocateVex(roadBean.startCross) == verTexId && LocateVex(roadBean.endCross) == preVerTexId) {
                    p = p.tlink;
                    continue;
                }

                int loc = LocateVex(roadBean.endCross);
                if (visited[loc] == 0 && d[loc] > d[k] + roadBean.roadLength) {//visited[loc] == 0 && d[loc] > d[k] + roadBean.roadLength Graph[loc].crossBusiness
                    d[loc] = d[k] + roadBean.roadLength;//d[loc] = d[k] + roadBean.roadLength;
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
     * 准备开始运行  那些还没开始运动的点
     *
     * @param carBean
     */

    private static void CheckThePreparingStartingRoad(CarBean carBean) {

        //当前车辆的map图
        int currRoadEndCross = carBean.startBean;

        for (ArcBox q = Graph[LocateVex(currRoadEndCross)].firstout; q != null; q = q.tlink) {
            //判断即将走的路是否能通行
            RoadBean roadBean = q.roadBean;

            LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
            //表示是否设置过visible标志位
            boolean isPass = false;
            for (int i = 0; i < carBeanQueues.length; i++) {
                LinkedList<CarBean> carBeanQueue = carBeanQueues[i];

                if (carBeanQueue.size() == 0) {
                    roadBean.visible = true;
                    isPass = true;
                    break;
                } else {
                    CarBean carBean1 = carBeanQueue.peekLast();
                    //此车正常结束
                    if (carBean1.s1 == roadBean.roadLength - 1) {
                        continue;
                    }
                    //能进入
                    roadBean.visible = true;
                    isPass = true;
                    break;
                }
            }

            if (!isPass) roadBean.visible = false;
        }


        //遍历其他边

        for (int i = 0; i < roadBeanList.size(); i++) {

            boolean isSet = false;
            RoadBean roadBean = roadBeanList.get(i);
            if (roadBean.startCross != currRoadEndCross) {//更新当前道路的所有状态信息
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
        }

        //扫描一遍所有的道路  求出其中相对长度
//        checkTheCrossBusiness();
    }


    /**
     * 检查边的状态 调度每一个等待状态的点时调整一次
     */

    private static void CheckTheRoadBusiness(CarBean carBean) {

        //当前车辆的map图
        int currRoadEndCross = carBean.currentRoadBean.endCross;
        for (ArcBox q = Graph[LocateVex(currRoadEndCross)].firstout; q != null; q = q.tlink) {
            //判断即将走的路是否能通行
            RoadBean roadBean = q.roadBean;

            LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;
            //表示是否设置过visible标志位
            boolean isPass = false;
            for (int i = 0; i < carBeanQueues.length; i++) {//每一个车道
                LinkedList<CarBean> carBeanQueue = carBeanQueues[i];

                if (carBeanQueue.size() == 0) {
                    roadBean.visible = true;
                    isPass = true;
                    break;
                } else {
                    CarBean carBean1 = carBeanQueue.peekLast();
                    if (carBean1.isWaiting) {//在等待
                        int s2 = roadBean.roadLength - carBean1.s1 - 1;
                        int v2 = Math.min(roadBean.speedLimit, carBean.maxSpeed);
                        if (v2 - carBean.s1 <= 0) {//不能通过
                            //TODO 此处应该有问题， 应该是true
                            roadBean.visible = true;
                            isPass = true;
                        } else if (v2 - carBean.s1 > s2) {//直接标注道路不可使用  可以用一个循环次数来标记
                            //roadBean.visible = (i == carBeanQueues.length - 1) ? false : true;//TODO 状态改变
                            roadBean.visible = true;
                            isPass = true;
                        } else {//能进入此道路
                            roadBean.visible = true;
                            isPass = true;
                        }
                        break;
                    } else {//此车正常结束

                        if (carBean1.s1 == roadBean.roadLength - 1) {
                            continue;
                        }
                        //能进入
                        roadBean.visible = true;
                        isPass = true;
                        break;
                    }

                }
            }

            if (!isPass) roadBean.visible = false;
        }


        //遍历其它不直接相关边
        for (int i = 0; i < roadBeanList.size(); i++) {

            boolean isSet = false;
            RoadBean roadBean = roadBeanList.get(i);
            if (roadBean.startCross != currRoadEndCross) {//更新当前道路的所有状态信息
                LinkedList<CarBean>[] carBeanQueues = roadBean.carBeanQueues;

                for (int k = 0; k < carBeanQueues.length; k++) {//每一个车道
                    LinkedList<CarBean> carBeanQueue = carBeanQueues[k];

                    if (carBeanQueue.size() == 0) {
                        roadBean.visible = true;
                        isSet = true;
                        break;
                    } else {
                        CarBean carBean1 = carBeanQueue.peekLast();

                        if (carBean1.isWaiting) {
                            //roadBean.visible = i == carBeanQueue.size() - 1 ? false : true;
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
        }
        //我需要设置每条交叉路口的拥堵情况
        //checkTheCrossBusiness();
    }

    /**
     * 设置每个交叉入口的拥堵情况
     *
     */
    /*private static void checkTheCrossBusiness() {
        for (int i = 0; i < Graph.length; i++) {
            int AllCarSize = 0;
            int CarRunningInTheCar = 0;
            for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {

                int[] nums = FindTheWaitingCarInLane(q.roadBean);
                if(nums.length!=2) throw new RuntimeException("长度数据异常");
                AllCarSize += nums[0];
                CarRunningInTheCar += nums[1];

            }
            Graph[i].crossBusiness = AllCarSize==0?0:CarRunningInTheCar / AllCarSize;
        }
    }*/

    /**
     * 计算道路上的等待车辆和总的车辆个数
     * @param roadBean  需要统计的道路
     * @return  返回数组  长度为2  AllCarSize  CarRunningInTheCar
     */
    public static int[] FindTheWaitingCarInLane(RoadBean roadBean) {

        int AllCarSize = 0;

        int CarRunningInTheCar = 0;
        for (List<CarBean> carBeans : roadBean.carBeanQueues) {
            AllCarSize += carBeans.size();
            for (int j = carBeans.size() - 1; j >= 0; j--) {
                CarBean carBean = carBeans.get(j);
                if(carBean.isWaiting) CarRunningInTheCar++;
                else break;
            }
        }
        return new int[]{AllCarSize, CarRunningInTheCar};
    }


    private static int LocateVex(int crossId) {

        for (int i = 0; i < Graph.length; i++) {

            if (Graph[i].crossId == crossId) {
                return i;
            }
        }

        throw new RuntimeException("内部数据错误");
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

    private static boolean isAllFinish() {

        for (int i = carBeanList.size() - 1; i >= 0; i--) {
            if (!carBeanList.get(i).isFinish) {
                return false;
            }
        }
        return true;
    }


    /**
     * 标志车辆现在的状态
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


                    if (carBean.currSpeed >= distance) {//等待转弯调度或者直接结束状态
                        if (index == 0) {//当前车是最前车
                            //carBean.isWaiting = carBean.currSpeed == distance ? false : true;
                            //当前正好到此道路的最前面
                            if (carBean.currSpeed == distance) {
                                carBean.isWaiting = false;
                                //设置距离
                                carBean.s1 = 0;
                            } else {//表示等待调度
                                carBean.isWaiting = true;
                            }

                            //当前车的最终目的地就是此刻对应的位置
                            if (roadBean.endCross == carBean.endBean) {
                                carBean.isWaiting = false;
                                carBean.isFinish = true;
                                count--;
                                carBean.visitedEdge[carBean.index] = -1;
                                carBeans.poll();
                                //当前值+1
                                index--;
                                continue;
                            }

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
    }

}

