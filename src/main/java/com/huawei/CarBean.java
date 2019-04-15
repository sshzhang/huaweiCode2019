package com.huawei;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    //访问过得道路id
    //public int[] visitedEdge;

    public List<Integer> visitedEdges;

    //指向相应的visitedEdge索引 默认为0
    public int index = 0;

    //已经设置过方向就不需要重新设置方向
    //此字段需要实时更新
    public boolean isSetDirection = false;

    //是否为优先车辆  1表示为优先车辆
    public int priorityCar;

    //是否为预制方向车辆  1表示是预制方向车辆
    public int directionCar;

    //表示当前车所在的车道编号,如果没上路设置为-1
    public int channel = -1;

    //表示当前车在某个方向上的等待调度次, 也就是下面边的等待次数
    //public int count = 0;

    //表示这辆车在起始点为currremtRoadBean.startCross 终止为endCrossIndex等待
    //public int endCrossIndex = -1;

    //死锁回溯的问题
    public CarBean(int carId, int startBean, int endBean, int maxSpeed, int startTime, int priorityCar, int directionCar) {
        this.carId = carId;
        this.startBean = startBean;
        this.endBean = endBean;
        this.maxSpeed = maxSpeed;
        this.startTime = startTime;
        visitedEdges = new ArrayList<>();
        this.priorityCar = priorityCar;
        this.directionCar = directionCar;
    }


    public CarBean(int carId, int startBean, int endBean, int maxSpeed, int startTime, boolean isStart, boolean isWaiting,
                   boolean isFinish, RoadBean currentRoadBean, int s1, int currSpeed, List<Integer> visitedEdges, int index,
                   boolean isSetDirection, int priorityCar, int directionCar, int channel) {
        this.carId = carId;
        this.startBean = startBean;
        this.endBean = endBean;
        this.maxSpeed = maxSpeed;
        this.startTime = startTime;
        this.isStart = isStart;
        this.isWaiting = isWaiting;
        this.isFinish = isFinish;
        this.currentRoadBean = currentRoadBean;
        this.s1 = s1;
        this.currSpeed = currSpeed;
        this.visitedEdges = visitedEdges;
        this.index = index;
        this.isSetDirection = isSetDirection;
        this.priorityCar = priorityCar;
        this.directionCar = directionCar;
        this.channel = channel;
    }
    /**
     * 优先级降序  预置车辆降序  出发时间升序   序列ID升序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(CarBean o) {
        int orderPriority = this.priorityCar - o.priorityCar;
        //int directionPriority = this.directionCar - o.directionCar;
        int orderTime = this.startTime - o.startTime;
        int orderId = this.carId - o.carId;
        return orderPriority != 0 ? -orderPriority : (orderTime != 0 ? orderTime : orderId);
    }
    /**
     * 运行上路车辆
     */
    public void runTheStartCar(VexNode[] Graph, int time) {

        //判断当前车的类型
        if (this.directionCar == 1||this.isSetDirection) {//当前车是预置车辆,可能也是优先车
            //拥堵情况可能出现无法上路, 预置车辆出发时间顺延
            RoadBean roadBean = null;
            for (ArcBox p = Graph[ArrangeTheCarForEnd.LocateVex(this.startBean)].firstout; p != null; p = p.tlink) {
                if (p.roadBean.roadId == this.visitedEdges.get(0)) {
                    roadBean = p.roadBean;
                    break;
                }
            }
            if (roadBean == null) throw new RuntimeException("数据格式异常");
            roadBean.runCarToRoad(this, time);

        }else if (this.priorityCar == 1) {//优先车辆
            //已修改, 添加当前车辆 @author wxq
            ArrangeTheCarForEnd.CheckThePreparingStartingRoad(this);
            //先计算相对最短路径 ，再计算绝对最短路径
            int[][] path = new int[Graph.length][Graph.length];
            int[] visited = new int[Graph.length];
            //到达调度时间
            ArrangeTheCarForEnd.ShortestPath_DJSTR(-1, ArrangeTheCarForEnd.LocateVex(this.startBean), path, visited, ArrangeTheCarForEnd.LocateVex(this.endBean));
            RoadBean roadBean = null;
            int nextCrossIndex = -1;
            if (visited[ArrangeTheCarForEnd.LocateVex(this.endBean)] == 0) {

                //throw new RuntimeException("此优先车没有启动");
                path = new int[Graph.length][Graph.length];
                ArrangeTheCarForEnd.ShortestPath_DJSTRAbSolute(-1, ArrangeTheCarForEnd.LocateVex(this.startBean), path, visited, ArrangeTheCarForEnd.LocateVex(this.endBean));
                if (visited[ArrangeTheCarForEnd.LocateVex(this.endBean)] == 0) throw new RuntimeException("数据格式异常");
                nextCrossIndex = path[ArrangeTheCarForEnd.LocateVex(this.endBean)][1];
            } else {
                nextCrossIndex = path[ArrangeTheCarForEnd.LocateVex(this.endBean)][1];
            }
            for (ArcBox p = Graph[ArrangeTheCarForEnd.LocateVex(this.startBean)].firstout; p != null; p = p.tlink) {
                if (ArrangeTheCarForEnd.LocateVex(p.roadBean.endCross) == nextCrossIndex) {
                    roadBean = p.roadBean;
                    break;
                }
            }
            if (roadBean == null) throw new RuntimeException("数据格式异常");
            //对于优先车辆,先设置好方向
            this.isSetDirection = true;
            this.visitedEdges.add(roadBean.roadId);
            //开始运行
            roadBean.runCarToRoad(this, time);

        } else {//普通车辆
            //已修改, 添加当前车辆, 用做计算权重
            ArrangeTheCarForEnd.CheckThePreparingStartingRoad(this);
            int[][] path = new int[Graph.length][Graph.length];
            int[] visited = new int[Graph.length];
            //到达调度时间
            ArrangeTheCarForEnd.ShortestPath_DJSTR(-1, ArrangeTheCarForEnd.LocateVex(this.startBean), path, visited, ArrangeTheCarForEnd.LocateVex(this.endBean));
            RoadBean roadBean = null;
            if (visited[ArrangeTheCarForEnd.LocateVex(this.endBean)] == 1) {
                for (ArcBox p = Graph[ArrangeTheCarForEnd.LocateVex(this.startBean)].firstout; p != null; p = p.tlink) {
                    if (ArrangeTheCarForEnd.LocateVex(p.roadBean.endCross) == path[ArrangeTheCarForEnd.LocateVex(this.endBean)][1]) {
                        roadBean = p.roadBean;
                        break;
                    }
                }
                if (roadBean == null) throw new RuntimeException("数据格式异常");

                roadBean.runCarToRoad(this, time);
            } /*else {
                throw new RuntimeException("此普通车没有启动");
            }*/
        }


    }
    /**
     * 深度拷贝当前车对象
     */
    public CarBean DeepCloneCarBean() {

        List<Integer> copyvisitedEdges = new ArrayList<>();
        for (Integer in : this.visitedEdges) {
            copyvisitedEdges.add(in);
        }

        return new CarBean(this.carId, this.startBean, this.endBean, this.maxSpeed, this.startTime, this.isStart,this.isWaiting,
                this.isFinish, this.currentRoadBean, this.s1, this.currSpeed, copyvisitedEdges, this.index,
                this.isSetDirection, this.priorityCar, this.directionCar, this.channel);
    }



    /**
     * 当前车已经完成，更新其它车辆
     * 更新当前车所在车道上的所有车辆信息
     */
    // public void afterFinishUpdateTheChannelCarStatus() {

    //LinkedList<CarBean> carBeanQueue = this.currentRoadBean.carBeanQueues[this.channel];

     /*   for (int i = 0; i < carBeanQueue.size(); i++) {

            CarBean carBean = carBeanQueue.get(i);

            if(!carBean.isWaiting) break;

            CarBean preCarBean = i == 0 ? null : carBeanQueue.get(i - 1);

            int currSpeed = Math.min(currentRoadBean.speedLimit, carBean.maxSpeed);

            currSpeed = i == 0 ? currSpeed : Math.min(currSpeed, carBean.s1 - preCarBean.s1 - 1);

            int distance = i == 0 ? carBean.s1 : carBean.s1 - preCarBean.s1 - 1;

            if (currSpeed > distance) {//不允许调度  可能会出路口
                break;
            } else {
                carBean.isWaiting = false;
                carBean.s1 -= currSpeed;
            }
        }*/


/*        for (int i = 0; i < carBeanQueue.size(); i++) {
            CarBean carBean = carBeanQueue.get(i);

            //TODO  更新后面车的数据最起码这辆车要是等待状态,如果不是直接break
            if (!carBean.isWaiting) break;

            //当前是第一辆车并且是优先级车辆  到达目的地
            if (i == 0 && carBean.priorityCar == 1 && carBean.endBean == this.currentRoadBean.endCross) {
                int speed = Math.min(this.maxSpeed, this.currentRoadBean.speedLimit);
                if (speed >= carBean.s1) {
                    carBean.isWaiting = false;
                    carBean.isFinish = true;
                    ArrangeTheCarForEnd.finishCarNums++;
                    ArrangeTheCarForEnd.allFinishedCarBeanList.add(carBean);
                    if (!ArrangeTheCarForEnd.allStartRunningCarBeanList.remove(carBean))
                        throw new RuntimeException("内部逻辑错误");
                    ArrangeTheCarForEnd.count--;
                    carBeanQueue.poll();
                    i--;
                } else {//不可达到目的地

                    carBean.isWaiting = false;
                    carBean.s1 -= speed;
                    ChangeWaitingToStop(this.currentRoadBean, carBeanQueue);
                    break;
                }
            } else {
                if (i == 0) {//当前车为非优先级或者没有到达目的地

                    //非优先级但到达目的地

                    //非优先级 没到达目的地

                    //优先级没达到目的地

                    int speed = Math.min(carBean.maxSpeed, this.currentRoadBean.speedLimit);
                    if (speed <= carBean.s1) {
                        carBean.isWaiting = false;
                        carBean.s1 -= speed;
                        ChangeWaitingToStop(this.currentRoadBean, carBeanQueue);
                    }
                    break;

                } else {
                    throw new RuntimeException("逻辑错误");
                }
            }
        }*/
    //}


    /**
     * 最前的一辆车因为速度原因不能转弯，刷新此车所在的车道中的其他车
     *
     * @param roadBean
     * @param carBeanQueue
     */
    protected void ChangeWaitingToStop(RoadBean roadBean, LinkedList<CarBean> carBeanQueue) {
        if(Constant.DEAD_LOCK_CHECK_FLAGE)
            ArrangeTheCarForEnd.currNoWaitingCarBeanList.add(this.DeepCloneCarBean());

        this.isWaiting = false;
        this.s1 = 0;
        //roadBean.waitingTheCarNums--;
        for (int k = 1; k < carBeanQueue.size(); k++) {
            CarBean carBean = carBeanQueue.get(k);
            if (!carBean.isWaiting) break;
            if (Constant.DEAD_LOCK_CHECK_FLAGE) {
                ArrangeTheCarForEnd.currNoWaitingCarBeanList.add(carBean.DeepCloneCarBean());
            }

            carBean.isWaiting = false;
            //roadBean.waitingTheCarNums--;
            CarBean preCarBean = carBeanQueue.get(k - 1);
            int speed = Math.min(Math.min(roadBean.speedLimit, carBean.maxSpeed), carBean.s1 - preCarBean.s1 - 1);
            carBean.s1 -= speed;
        }
    }


    /**
     * 当前车辆移动到下一个道路
     *
     * @param nextRoadBean 下一个道路
     * @return true 成功移动， 否则失败
     */
    public boolean moveToNextRoad(RoadBean nextRoadBean) {

        //判断即将转到的新道路情况
        LinkedList<CarBean>[] carBeanQueuesnext = nextRoadBean.carBeanQueues;
        int v2 = Math.min(this.maxSpeed, nextRoadBean.speedLimit);
        int s1 = this.s1;
        //等待初始化
        int s2 = -1;
        //判断是否调度成功
        boolean isSuccess = false;
        //用来标志是否设置过当前等待车辆的状态  如果没设置表明当前车只能等待
        boolean isFlages = false;
        //提前保存当前车辆的道路信息和队列信息
        LinkedList<CarBean> currCarBeanQueue = this.currentRoadBean.carBeanQueues[this.channel];
        RoadBean currentRoadBean = this.currentRoadBean;
        int channel = this.channel;
        if (this.endBean == this.currentRoadBean.endCross || !this.isStart || !this.isWaiting)
            throw new RuntimeException("程序内部逻辑错误");
        for (int j = 0; j < carBeanQueuesnext.length; j++) {
            CarBean carBeanNext = carBeanQueuesnext[j].peekLast();
            if (carBeanNext == null) {
                if (v2 - s1 <= 0) {//速度没达到要求， 直接在当前道路的最前面停止
                    ChangeWaitingToStop(currentRoadBean, currCarBeanQueue);
                    //这种情况也算移动成功
                    return true;
                } else {//能够通过
                    isSuccess = true;
                    isFlages = true;
                    turnToNextRoad(nextRoadBean, v2, j);
                    break;
                }
            }
            //没有任何实际的作用,只是为了检测异常逻辑
            if (carBeanNext.s1 > nextRoadBean.roadLength - 1) {
                //System.out.println(carBeanNext);
                throw new RuntimeException("车辆数据长度异常");
            }
            //当前车辆的转弯方向有车辆等待
            if (carBeanNext.isWaiting) {
                //判断速度是否能通过
                if (v2 - this.s1 <= 0) {//不能通过
                   /* this.s1 = 0;
                    currentRoadBean.waitingTheCarNums--;
                    this.isWaiting = false;*/
                    ChangeWaitingToStop(currentRoadBean, currCarBeanQueue);
                    return true;
                } else {//可能会通过
                    s2 = nextRoadBean.roadLength - carBeanNext.s1 - 1;

                    if (v2 - this.s1 <= s2) {//可以转入
                        isSuccess = true;
                        isFlages = true;
                        turnToNextRoad(nextRoadBean, v2, j);
                        break;
                    } else {//不能通过阻挡车辆
                        return false;
                    }
                }
            } else {//前车是终止状态
                //当前车道堵住
                if (carBeanNext.s1 == nextRoadBean.roadLength - 1) {
                    continue;
                } else {
                    v2 = Math.min(v2, nextRoadBean.roadLength - carBeanNext.s1 - 1 + this.s1);
                    if (v2 - this.s1 <= 0) {//不允许转弯
                        ChangeWaitingToStop(currentRoadBean, currCarBeanQueue);
                        return true;
                    } else {//允许转弯
                        isSuccess = true;
                        isFlages = true;
                        turnToNextRoad(nextRoadBean, v2, j);
                        break;
                    }
                }
            }
        }
        if (isSuccess) {//更新转弯之后车道数据
            //调度当前车辆 只调度不出路口的车辆
            ArrangeTheCarForEnd.FlageTheWaitingCar(currentRoadBean, channel);
       /*     for (int k = 0; k < currCarBeanQueue.size(); k++) {

                CarBean carBean = currCarBeanQueue.get(k);

                if (!carBean.isWaiting) break;
                CarBean preCarBean = k == 0 ? null : currCarBeanQueue.get(k - 1);


                int currSpeed = Math.min(currentRoadBean.speedLimit, carBean.maxSpeed);

                currSpeed = k == 0 ? currSpeed : Math.min(currSpeed, carBean.s1 - preCarBean.s1 - 1);

                int distance = k == 0 ? carBean.s1 : carBean.s1 - preCarBean.s1 - 1;

                if (currSpeed > distance) {//不允许调度  可能会出路口
                    break;
                } else {
                    carBean.isWaiting = false;
                    carBean.s1 -= currSpeed;
                }

            }*/


         /*   for (int k = 0; k <= currCarBeanQueue.size() - 1; k++) {
                CarBean carBeanCurr = currCarBeanQueue.get(k);
                CarBean precarBeanCurr = k != 0 ? currCarBeanQueue.get(k - 1) : null;
                if (!carBeanCurr.isWaiting) break;
                int currSpeed = Math.min(currentRoadBean.speedLimit, carBeanCurr.maxSpeed);
                //不需要调整 存在出入口
                if (precarBeanCurr == null && currSpeed >= carBeanCurr.s1) {
                    //当前车是优先车 且当前车到达目的地(需要确保不会发生阻挡)
                    if (carBeanCurr.endBean == currentRoadBean.endCross && carBeanCurr.priorityCar == 1) {
                        carBeanCurr.isWaiting = false;
                        carBeanCurr.isFinish = true;
                        ArrangeTheCarForEnd.finishCarNums++;
                        ArrangeTheCarForEnd.count--;
                        carBeanCurr.s1 = 0;
                        ArrangeTheCarForEnd.allFinishedCarBeanList.add(carBeanCurr);
                        ArrangeTheCarForEnd.allStartRunningCarBeanList.remove(carBeanCurr);
                        currCarBeanQueue.poll();
                        k--;
                        continue;
                    } else { //车辆没有到达目的地或者车辆是普通车

                        //车辆没有到达目的地   车辆是普通车

                        // 车辆没有到达目的地  优先车辆

                        //车辆到达目的地  普通车辆

                        //车辆没有到达目的地且距离正好合适
                        if (carBeanCurr.endBean != currentRoadBean.endCross && currSpeed == carBeanCurr.s1) {
                            carBeanCurr.isWaiting = false;
                            carBeanCurr.s1 = 0;
                            continue;
                        }
                        //存在转弯的点 直接结束
                        break;
                    }
                    //相等按照下面处理
                }
                //不能跳过
                int distance = precarBeanCurr == null ? carBeanCurr.s1 : carBeanCurr.s1 - precarBeanCurr.s1 - 1;
                currSpeed = Math.min(currSpeed, distance);
                carBeanCurr.currSpeed = currSpeed;
                carBeanCurr.s1 = carBeanCurr.s1 - currSpeed;
                carBeanCurr.isWaiting = false;
            }*/
        }

        if (!isFlages) {//在拥堵额情况下还是需要设置数据
            ChangeWaitingToStop(currentRoadBean, currCarBeanQueue);
            return true;
        }
        return isSuccess;
    }

    /**
     * 转弯当前车辆
     *
     * @param nextRoadBean 下一条道路
     * @param v2           速度
     * @param j            当前车要进入的下一条道路上的车道号
     */
    private void turnToNextRoad(RoadBean nextRoadBean, int v2, int j) {



        if(Constant.DEAD_LOCK_CHECK_FLAGE)
            ArrangeTheCarForEnd.currNoWaitingCarBeanList.add(this.DeepCloneCarBean());
        //转弯 当前道路车辆数和等待车辆数减一
        this.isWaiting = false;
        //移除这辆车
        if (this.currentRoadBean.carBeanQueues[this.channel].poll() == null) throw new RuntimeException("程序内部逻辑错误");
        this.currentRoadBean = nextRoadBean;
        this.channel = j;
        this.s1 = nextRoadBean.roadLength - (v2 - this.s1);
        this.isSetDirection = false;
        this.currentRoadBean.carBeanQueues[this.channel].add(this);
        //设置预置方向当前走到的道路id
        if (this.directionCar == 1) this.index++;
    }


    public void CopyALLValue(CarBean carBean) {
        this.carId = carBean.carId;
        this.startBean = carBean.startBean;
        this.endBean = carBean.endBean;
        this.maxSpeed = carBean.maxSpeed;
        this.startTime = carBean.startTime;
        this.isStart = carBean.isStart;
        this.isWaiting = carBean.isWaiting;
        this.isFinish = carBean.isFinish;
        this.currentRoadBean = carBean.currentRoadBean;
        this.s1 = carBean.s1;
        this.currSpeed = carBean.currSpeed;
        //this.visitedEdges = carBean.visitedEdges;
        this.visitedEdges.clear();
        for (Integer in : carBean.visitedEdges) {
            this.visitedEdges.add(in);
        }
        this.index = carBean.index;
        this.isSetDirection = carBean.isSetDirection;
        this.priorityCar = carBean.priorityCar;
        this.directionCar = carBean.directionCar;
        this.channel = carBean.channel;
    }

}
