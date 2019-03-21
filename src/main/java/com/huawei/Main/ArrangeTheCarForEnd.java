package com.huawei.Main;

import com.huawei.beans.*;
import com.huawei.utils.BuildTheMapUtil;
import com.huawei.utils.ReaddingTheData;

import java.util.LinkedList;

/**
 * 车辆调度类
 */
public class ArrangeTheCarForEnd {


    private static VexNode[] Graph = null;

    static {
        try {
            Class.forName(BuildTheMapUtil.class.getName(), true, Thread.currentThread().getContextClassLoader());
            Graph = BuildTheMapUtil.Graph;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {

        ArrangeTheCar();
    }


    public static void ArrangeTheCar() {


        int time = 0;

        while (!isAllFinish()) {

            System.out.println(time);
            //划分清楚在等待车辆和终止状态车辆
            System.out.println("FlageTheWaitingCar");
            FlageTheWaitingCar();
            System.out.println("RunTheWaitingCar");
            RunTheWaitingCar();
            //运行车库中车辆
            System.out.println("driverAllCarInGarage");
            driverAllCarInGarage(time);
            time++;
        }

        System.out.println("打印出调度数据");


        for (int i = 0; i < ReaddingTheData.carBeanList.size(); i++) {

            CarBean carBean = ReaddingTheData.carBeanList.get(i);

            StringBuilder builder = new StringBuilder();

            builder.append(carBean.carId + ", " + carBean.startTime );

            for (int index = 0; index < carBean.index; index++) {
                builder.append(", " + carBean.visitedEdge[index]);
            }
            System.out.println(builder.toString());

        }


    }

    //开启车库中的车
    private static void driverAllCarInGarage(int time) {

        //启动每一个未开启的车辆
        for (int k = 0; k < ReaddingTheData.carBeanList.size(); k++) {
            CarBean carBeanr = ReaddingTheData.carBeanList.get(k);
            //车没有调度完成
            if (!carBeanr.isFinish && !carBeanr.isStart) {

                //调度没有运行的车辆
                CarBean carBeanNotRunning = ReaddingTheData.carBeanList.get(k);
                //没有到调度时间
                if (carBeanNotRunning.startTime > time - 1) continue;

                //重新计算边的情况
                CheckThePreparingStartingRoad(carBeanNotRunning);
                int[][] path = new int[Graph.length][Graph.length];
                int[] visited = new int[Graph.length];
                //到达调度时间
                ShortestPath_DJSTR(-1,LocateVex(carBeanNotRunning.startBean), path, visited);

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
                                        carBeanNotRunning.isStart = true;
                                        carBeanNotRunning.currentRoadBean = q.roadBean;
                                        carBeanNotRunning.currSpeed = Math.min(Math.min(carBeanNotRunning.maxSpeed, roadBean.speedLimit), roadBean.roadLength - carBean.s1 - 1);
                                        carBeanNotRunning.isWaiting = false;
                                        carBeanNotRunning.visitedEdge[carBeanNotRunning.index++] = q.roadBean.roadId;
                                        carBeanNotRunning.startTime = time - 1;
                                        //设置距离
                                        carBeanNotRunning.s1 = roadBean.roadLength-carBeanNotRunning.currSpeed;
                                        carBeanQueues[i].add(carBeanNotRunning);
                                        iset = true;
                                        break;
                                    }
                                }
                            }

                            if (!iset) System.out.println("车开启调度失败");


                        }
                    }

                    if (!carBeanNotRunning.isStart) System.out.println("内部数据错误");
                    //throw new RuntimeException("内部数据错误");

                }

            }
        }
    }


    private static void RunTheWaitingCar() {
        int count = 0;
        //调度等待车辆
        while (!isAllNormalStopStatus()) {
            count++;
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
                            //roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                            index++;
                            continue;
                        }
                        CarBean peek = carBeanQueue.peek();
                        //TODO 等待检验
                        if (!peek.isWaiting) {//下一个车道调度
                            index += 1;
                            continue;
                        }
                        //检查边的状态 调度每一个等待状态的点时调整一次
                        CheckTheRoadBusiness(peek);
                        int[][] path = new int[Graph.length][Graph.length];
                        int[] visited = new int[Graph.length];
                        //LocateVex(peek.endBean) 表示从peek.currentRoadBean.endCross==roadBean.endCross到其它节点的距离
                        ShortestPath_DJSTR(LocateVex(peek.currentRoadBean.startCross),LocateVex(peek.currentRoadBean.endCross), path, visited);
                        //当前路径不可达停止  当前车辆到达最后的位置
                        if (visited[LocateVex(peek.endBean)] == 0 || peek.endBean == roadBean.endCross) {//当前节点不可达
                            //停止  更新所有节点


                            //TODO 更新当前数据信息  借结束状态
                            if (peek.endBean == roadBean.endCross) {//调度完成

                                int speed = Math.min(peek.maxSpeed, roadBean.speedLimit);
                                if (speed >= peek.s1) {//当前能进入
                                    //完成
                                    peek.isWaiting = false;
                                    peek.isFinish = true;
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
                                                //保存结束状态-1
                                                carBean.visitedEdge[carBean.index] = -1;
                                                carBeanQueue.poll();
                                                k--;
                                            }else{//不可到达
                                                carBean.isWaiting = false;
                                                carBean.s1 -= speed;
                                                ChangeWaitingToStop(roadBean, carBeanQueue);
                                                index++;
                                                break;
                                            }

                                        } else {

                                            if(k==0){//没有到目的点
                                                speed = Math.min(carBean.maxSpeed, roadBean.speedLimit);
                                                if (speed <=carBean.s1) {
                                                    carBean.isWaiting = false;
                                                    carBean.s1 -= speed;
                                                    ChangeWaitingToStop(roadBean, carBeanQueue);
                                                }
                                                break;
                                            }else{//应该不存在
                                                throw new RuntimeException("逻辑错误");
                                            }
                                        }
                                    }

                                } else {//可能进入

                                    peek.isWaiting = false;
                                    peek.s1 -= speed;

                                    ChangeWaitingToStop(roadBean, carBeanQueue);
                                }
                                index++;
                                continue;

                            }


                            peek.isWaiting = false;
                            for (int n = 1; n < carBeanQueue.size(); n++) {
                                CarBean carBean = carBeanQueue.get(n);

                                if (!carBean.isWaiting) break;

                                int distance = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), carBean.s1 - carBeanQueue.get(n - 1).s1 - 1);
                                carBean.isWaiting = false;
                                carBean.s1 -= distance;
                            }
                            //不会妨碍下一个车道上的车辆  , 上面只会妨碍当前车道上的车辆
                            index++;
                            continue;
                        } else {//可达
                            //开始调度

                            int nStartCrossIndex = LocateVex(roadBean.endCross);
                            int EndCrossIndex = path[LocateVex(peek.endBean)][1];
                            RoadBean nextRoadBean = null;
                            for (ArcBox q = Graph[nStartCrossIndex].firstout; q != null; q = q.tlink) {
                                if (LocateVex(q.roadBean.endCross) == EndCrossIndex) {
                                    nextRoadBean = q.roadBean;
                                    break;
                                }
                            }


                            //assert nextRoadBean != null;
                            if (nextRoadBean == null) throw new RuntimeException("内部数据异常!");
                            // roadBean.startCross---->roadBean.endCross---->nextRoadBean.endCross

                            //调头
                            if (LocateVex(roadBean.startCross) == EndCrossIndex) {

                                peek.isWaiting = false;
                                for (int n = 1; n < carBeanQueue.size(); n++) {
                                    CarBean carBean = carBeanQueue.get(n);
                                    if (!carBean.isWaiting) {
                                        break;
                                    }
                                    int distance = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), carBean.s1 - carBeanQueue.get(n - 1).s1 - 1);
                                    carBean.isWaiting = false;
                                    carBean.s1 -= distance;
                                }

                                //不会妨碍下一个车道上的车辆  , 上面只会妨碍当前车道上的车辆
                                //roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                                index++;
                                continue;
                            }

                            int direction = FindTheDirection(LocateVex(roadBean.startCross), nStartCrossIndex, EndCrossIndex);


                            //获得转弯之后的那个道路
                            if (direction == Constant.STRAIGHT) {
                                //转弯成功

                                waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean, count);
                                index++;
                                continue;


                            } else if (direction == Constant.LEFT) {
                                //直行道路
                                RoadBean strRoadBean = null;
                                //TODO 对于方向问题有点难理解
                                int newDirection = p.headDirection - 1 < 0 ? (4 + p.headDirection - 1) % 4 : (p.headDirection - 1) % 4;
                                for (ArcBox q = Graph[i].firstin; q != null; q = q.hlink) {
                                    if (q.headDirection == newDirection) { //找出直行对应的道路
                                        strRoadBean = q.roadBean;
                                        break;
                                    }
                                }

                                if (strRoadBean != null && BlockingFlages(strRoadBean, Constant.STRAIGHT)) {
                                    index++;//继续下一个车道
                                    continue;
                                }


                                waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean, count);
                                index++;
                                continue;


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
                                    index++;
                                    continue;
                                }
                                //找出左转对应的道路
                                if (leftRoadBean != null && BlockingFlages(leftRoadBean, Constant.LEFT)) {
                                    index++;
                                    continue;
                                }
                                //如果都不存在  开始调度
                                /*if (waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean, count)) {
                                    roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                                    continue;
                                } else {
                                    break;
                                }*/
                                waitingCarChangeDirection(roadBean, carBeanQueue, peek, nextRoadBean, count);
                                index++;
                                continue;
                            } else {
                                throw new RuntimeException("方向有问题!!!");
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


    private static boolean BlockingFlages(RoadBean strRoadBean, int direction) {
        boolean isBlock = false;

        LinkedList<CarBean>[] carBeanQueues = strRoadBean.carBeanQueues;

        for (int i = strRoadBean.index; i < carBeanQueues.length; i++) {

            LinkedList<CarBean> carBeanQueue = carBeanQueues[i];
            if (carBeanQueue.size() == 0) {//为0
                continue;
            } else {//车道上的数据不为空
                CarBean peek = carBeanQueue.peek();
                if (!peek.isWaiting) continue;
                CheckTheRoadBusiness(peek);
                int[][] path = new int[Graph.length][Graph.length];
                int[] visited = new int[Graph.length];
                ShortestPath_DJSTR(LocateVex(peek.currentRoadBean.startCross),LocateVex(peek.currentRoadBean.endCross), path, visited);
                //System.out.println("peek.endBean: " + peek.endBean);
                //LocateVex(peek.currentRoadBean.endCross)==LocateVex(peek.endBean) 此车正好在目的地 不会阻挡其它车辆
                if (visited[LocateVex(peek.endBean)] == 0 || LocateVex(peek.currentRoadBean.endCross)==LocateVex(peek.endBean)||!isInTheSameDirection(LocateVex(peek.currentRoadBean.startCross), LocateVex(peek.currentRoadBean.endCross), path[LocateVex(peek.endBean)][1], direction)) {
                    return false;
                } else return true;
            }
        }
        return isBlock;
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
    private static boolean waitingCarChangeDirection(RoadBean roadBean, LinkedList<CarBean> carBeanQueue, CarBean carBean, RoadBean nextRoad, int count) {

        System.out.println(carBean.carId + "   " + carBean.startBean + "    " + carBean.endBean + " " + carBean.currentRoadBean.roadId + "  ");
        //判断即将转到的新道路情况
        LinkedList<CarBean>[] carBeanQueuesnext = nextRoad.carBeanQueues;
        //判断是否调度成功
        boolean isSuccess = false;
        //v2可能的速度
        int v2 = Math.min(carBean.maxSpeed, nextRoad.speedLimit);
        int s1 = carBean.s1;
        //等待初始化
        int s2 = -1;
        boolean isFlages = false;
        for (int j = 0; j < carBeanQueuesnext.length; j++) {
            CarBean carBeanNext = carBeanQueuesnext[j].peekLast();
            if (carBeanNext == null) {//此队列为空
                //调度成功  更新相应数据
                //存在速度导致调度不成功
                carBean.isWaiting = false;
                if (v2 - s1 <= 0) {//说明不能通过入口
                    carBean.s1 = 0;
                    //更新当前车道上所有等待车辆信息
                    //为什么从k=0 开始呢 因为k=0标识队列的头  我们需要先更新最早抵达的车辆
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

                } else {
                    //查找当前道路的下一条车道
                    //roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                    isSuccess = true;
                    carBean.currentRoadBean = nextRoad;
                    carBean.visitedEdge[carBean.index++] = nextRoad.roadId;
                    //s2最大运行的距离
                    int maxS2 = v2 - s1;
                    //默认只能通过一个道路 数据为正
                    carBean.s1 = nextRoad.roadLength - maxS2;
                    carBean.currSpeed = v2;
                    //TODO 判断是否结束
                    //从原来队列中删除元素
                    carBeanQueue.poll();
                    //保存元素到最新队列
                    carBeanQueuesnext[j].add(carBean);
                }
                //不需要再调度
                isFlages = true;
                break;
            }

            if (carBeanNext.s1 >= nextRoad.roadLength - 1) {//此车占着位置
                if (carBeanNext.isWaiting) {//车道正在等待,说明当前车无法调度 依然处于等待状态
             /*       if (count >= 4) {
                        carBean.isWaiting = false;

                        for (int k = 1; k < carBeanQueue.size(); k++) {
                            CarBean carBean1 = carBeanQueue.get(k);

                            if (!carBean1.isWaiting) {
                                break;
                            }
                            int distance = Math.min(carBean1.s1 - carBeanQueue.get(k - 1).s1 - 1, Math.min(roadBean.speedLimit, carBean1.maxSpeed));
                            carBean1.isWaiting = false;
                            carBean1.s1 -= distance;
                        }
                    }*/
                   // isFlages = true;
                   // break;
                //} else {//道路下一个车道 说明车到已满. 需要换一条车道容纳当前车辆

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
                        for (int k = 1; k <= carBeanQueue.size() - 1; k++) {
                            CarBean carBeanChange = carBeanQueue.get(k);
                            if (!carBeanChange.isWaiting) {
                                break;
                            }
                            CarBean precarBeanChage = carBeanQueue.get(k - 1);
                            int distance = carBeanChange.s1 - precarBeanChage.s1 - 1;
                            //当前速度道路限速和终止状态的距离   最小值最为最大行驶速度
                            int maxValue = Math.min(roadBean.speedLimit, distance);
                            carBeanChange.s1 = carBeanChange.s1 - maxValue;
                            carBeanChange.isWaiting = false;
                        }

                        isFlages = true;
                        break;
                    }

                    //不可通过  可能存在死锁
                    if (v2 - carBean.s1 > s2) {//超过允许通行长度   等待再次调度
                        isFlages = true;
                        break;
                    } else {//可以通过
                        isSuccess = true;
                        carBean.s1 = nextRoad.roadLength - (v2 - carBean.s1);
                        carBean.currentRoadBean = nextRoad;
                        carBean.visitedEdge[carBean.index++] = nextRoad.roadId;
                        carBean.currSpeed = v2;
                        carBeanQueue.poll();
                        carBeanQueuesnext[j].add(carBean);
                        //carBean.index++;
                        carBean.isWaiting = false;
                        //查找当前道路的下一条车道
                        //roadBean.index = (roadBean.index + 1) % roadBean.roadNums;
                        isFlages = true;
                        break;

                    }
                } else {//终止状态
                    // carBean最多允许运行的距离
                    //当前道路运行的长度
                    v2 = Math.min(v2, carBean.s1 + nextRoad.roadLength - carBeanNext.s1 - 1);
                    //共同使用到数据
                    carBean.isWaiting = false;
                    if (v2 - carBean.s1 <= 0) {//不能通过
                        carBean.s1 = 0;
                        //更新当前车道上所有等待车辆信息
                        for (int k = 1; k <= carBeanQueue.size() - 1; k++) {
                            CarBean carBeanChange = carBeanQueue.get(k);
                            if (!carBeanChange.isWaiting) {
                                break;
                            }
                            CarBean precarBeanChage = carBeanQueue.get(k - 1);
                            int distance = carBeanChange.s1 - precarBeanChage.s1 - 1;
                            //当前速度道路限速和终止状态的距离   最小值最为最大行驶速度
                            int maxValue = Math.min(roadBean.speedLimit, distance);
                            carBeanChange.s1 = carBeanChange.s1 - maxValue;
                            carBeanChange.isWaiting = false;
                        }

                    } else {

                        //能通过
                        isSuccess = true;
                        carBean.s1 = nextRoad.roadLength - (v2 - carBean.s1);
                        carBean.currentRoadBean = nextRoad;
                        carBean.currSpeed = v2;

                        carBean.visitedEdge[carBean.index++] = nextRoad.roadId;

                        carBeanQueue.poll();
                        //carBean.index++;
                        carBeanQueuesnext[j].add(carBean);
                    }
                    isFlages = true;
                    break;

                }
            }
        }
        if (isSuccess) {//调度给定道路的某条车道
            //调度当前车道  只调度不出路口的车辆
            for (int k = 0; k <= carBeanQueue.size() - 1; k++) {
                CarBean carBeanCurr = carBeanQueue.get(k);
                CarBean precarBeanCurr = k != 0 ? carBeanQueue.get(k - 1) : null;

                if (!carBeanCurr.isWaiting) break;
                int currSpeed = Math.min(roadBean.speedLimit, carBeanCurr.maxSpeed);
                //不需要调整 存在出入口
                if (precarBeanCurr == null && currSpeed > carBeanCurr.s1) break;

                int distance = precarBeanCurr == null ? carBeanCurr.s1 : carBeanCurr.s1 - precarBeanCurr.s1 - 1;
                currSpeed = Math.min(currSpeed, distance);
                carBeanCurr.currSpeed = currSpeed;
                carBeanCurr.s1 = carBeanCurr.s1 - currSpeed;
                carBeanCurr.isWaiting = false;
            }
        }


        if (!isFlages) {//调度到最后停止当前车

            carBean.isWaiting = false;

            for (int k = 1; k < carBeanQueue.size(); k++) {
                CarBean carBean1 = carBeanQueue.get(k);

                if (!carBean1.isWaiting) {
                    break;
                }
                int distance = Math.min(carBean1.s1 - carBeanQueue.get(k - 1).s1 - 1, Math.min(roadBean.speedLimit, carBean1.maxSpeed));
                carBean1.isWaiting = false;
                carBean1.s1 -= distance;
            }


        }

        return isSuccess;
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
     * @param verTexId 表示交叉路口的索引id  待计算verTexId 到其它所有点的最短距离
     * @param path     路径数据
     * @param visited  是否存在最短路径
     */
    public static void ShortestPath_DJSTR(int preVerTexId, int verTexId, int path[][], int[] visited) {


        //设置 preVerTexId--->verTexId 为空



        //设置当前道路的反向道路不可用 不允许调头
        if(preVerTexId!=-1)
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
            if(!next.roadBean.visible) {next=next.tlink;continue;}
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
            while (p != null ) {//&& (preRoadBean == null || preRoadBean.roadId != p.roadBean.roadId)
                RoadBean roadBean = p.roadBean;

                if(!roadBean.visible) {p = p.tlink;continue;}

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


        for (int i = 0; i < ReaddingTheData.roadBeanList.size(); i++) {

            boolean isSet = false;
            RoadBean roadBean = ReaddingTheData.roadBeanList.get(i);
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
                            roadBean.visible = false;
                            isPass = true;
                        } else if (v2 - carBean.s1 > s2) {//直接标注道路不可使用  可以用一个循环次数来标记
                            roadBean.visible =(i==carBeanQueues.length-1)? false:true;//TODO 状态改变
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
        for (int i = 0; i < ReaddingTheData.roadBeanList.size(); i++) {

            boolean isSet = false;
            RoadBean roadBean = ReaddingTheData.roadBeanList.get(i);
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
                            roadBean.visible = i == carBeanQueue.size() - 1 ? false : true;
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

        for (int i = 0; i < ReaddingTheData.carBeanList.size(); i++) {
            if (ReaddingTheData.carBeanList.get(i).isWaiting) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAllFinish() {

        for (int i = ReaddingTheData.carBeanList.size()-1; i >=0; i--) {
            if (!ReaddingTheData.carBeanList.get(i).isFinish) {
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
        for (int i = 0; i < ReaddingTheData.roadBeanList.size(); i++) {//总共的道路数
            //扫描所有的车道
            RoadBean roadBean = ReaddingTheData.roadBeanList.get(i);
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


                    if (carBean.currSpeed >=distance) {//等待转弯调度或者直接结束状态
                        if (index == 0) {//当前车是最前车
                            //carBean.isWaiting = carBean.currSpeed == distance ? false : true;
                            //当前正好到此道路的最前面
                            if (carBean.currSpeed == distance) {
                                carBean.isWaiting = false;
                                //设置距离
                                carBean.s1 = 0;
                            }else{//表示等待调度
                                carBean.isWaiting = true;
                            }

                            //当前车的最终目的地就是此刻对应的位置
                            if (roadBean.endCross == carBean.endBean) {
                                carBean.isWaiting = false;
                                carBean.isFinish = true;
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
