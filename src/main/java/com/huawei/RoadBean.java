package com.huawei;

import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * 路径bean
 */
public class RoadBean {


    //道路id
    public int roadId;
    //道路长度
    public int roadLength;
    //最高限速
    public int speedLimit;
    //车道数目
    public int roadNums;

    //起始的交叉路口
    public int startCross;

    //终止的交叉路口
    public int endCross;

    //是否是双向的数据
    public int isBothWay;

    public boolean visible = true;

    //道路的相对拥堵情况, 默认为0 等于 道路长度-0.2*道路允许的速度+车的个数/车道数
    public double relativeRoadBlock;


    //惩罚权重
    public double penaltyWeight = 0;


    //容量
    //public double crossBusiness;

    //这条道路上所有车的个数
    //public int allTheCarNums = 0;

    //这条到路等待车辆的个数
    //public int waitingTheCarNums = 0;

    //指向当前调度的队列指针
    public int index = 0;


    public LinkedList<CarBean>[] carBeanQueues;

    //TODO 设置每条边对应的优先队列
    public LinkedList<CarBean> carBeanPriorityQueues;


    public RoadBean(int roadId, int roadLength, int speedLimit, int roadNums, int startCross, int endCross, int isBothWay, boolean visible) {
        this.roadId = roadId;
        this.roadLength = roadLength;
        this.speedLimit = speedLimit;
        this.roadNums = roadNums;
        this.startCross = startCross;
        this.endCross = endCross;
        this.isBothWay = isBothWay;
        this.visible = visible;
        this.carBeanQueues = new LinkedList[roadNums];
        for (int i = 0; i < this.roadNums; i++) {
            carBeanQueues[i] = new LinkedList<CarBean>();
        }
        this.carBeanPriorityQueues = new LinkedList<CarBean>();
    }

    /**
     * 判断车辆carCurrBean是否可以在当前道路运行
     * 如果能运行直接处理就行。
     *
     * @param carCurrBean
     */
    public void runCarToRoad(CarBean carCurrBean, int time) {
        //判断当前道路的拥堵情况
        LinkedList<CarBean>[] carBeanQueues = this.carBeanQueues;
        carCurrBean.currSpeed = Math.min(this.speedLimit, carCurrBean.maxSpeed);
        for (int i = 0; i < carBeanQueues.length; i++) {
            //当前车道为空
            if (carBeanQueues[i].size() == 0) {
                //开启
               /* carCurrBean.isStart = true;
                carCurrBean.s1 = this.roadLength - carCurrBean.currSpeed;
                carCurrBean.channel = i;
                carCurrBean.currentRoadBean = this;
                //当前车辆的方向索引
                if (carCurrBean.directionCar == 1) carCurrBean.index++;
                else carCurrBean.visitedEdges.add(this.roadId);
                //总共在运行的车辆个数
                ArrangeTheCarForEnd.count++;
                //把车添加到队列上
                carBeanQueues[i].add(carCurrBean);
                //this.allTheCarNums++;
                //只设置非预置车辆
                if (carCurrBean.directionCar == 0)
                    carCurrBean.startTime = time;

                //设置当前车辆开启
                ArrangeTheCarForEnd.allStartRunningCarBeanList.add(carCurrBean);
                //更新还未开启的车辆
                if(!ArrangeTheCarForEnd.allUnStartCarBeanList.remove(carCurrBean))
                    throw new RuntimeException("移除失败!!!");*/
                //开启车辆carCurrBean,更新相应的数据信息
                StartTheCar(carCurrBean, time, carBeanQueues[i], i);


                //return true;
                break;
            } else {//不为空
                //最后一辆车调度处理出来
                CarBean carBean = carBeanQueues[i].peekLast();

                int distance = this.roadLength - carBean.s1 - 1;

                if (carBean.isWaiting) {//前一辆车是等待状态
                    //下一辆车最多能走的距离
                    if (carCurrBean.currSpeed <= distance) {//当前车的速度小于两者之间的间隔
                        /*carCurrBean.isStart = true;
                        carCurrBean.channel = i;
                        carCurrBean.s1 = this.roadLength - carCurrBean.currSpeed;
                        carCurrBean.currentRoadBean = this;
                        if (carCurrBean.directionCar == 1) carCurrBean.index++;
                        else carCurrBean.visitedEdges.add(this.roadId);
                        carBeanQueues[i].add(carCurrBean);
                        ArrangeTheCarForEnd.count++;
                        //只设置非预置车辆
                        if (carCurrBean.directionCar == 0)
                            carCurrBean.startTime = time;
                        //设置当前车辆开启
                        ArrangeTheCarForEnd.allStartRunningCarBeanList.add(carCurrBean);

                        if( ArrangeTheCarForEnd.allUnStartCarBeanList.remove(carCurrBean))
                            throw new RuntimeException("移除失败!!!");*/
                        StartTheCar(carCurrBean, time, carBeanQueues[i], i);


                        //return true;

                    } else {// TODO 安排的数据调度有问题  没有考虑既是预置车辆 又是优先车辆
                       /* if (carCurrBean.directionCar == 1&&carCurrBean.priorityCar==1)
                            throw new RuntimeException("出现异常数据");*/
                        //return false;
                    }
                    //else  也是直接break 不开启当前车
                    break;
                } else {//前一辆车是终止状态
                    if (carBean.s1 > this.roadLength - 1) {
                        throw new RuntimeException("内部逻辑错误!");
                    }
                    if (carBean.s1 == this.roadLength - 1) {// 前车是终止状态
                        continue;
                    } else {
                        //获取当前车的速度
                        carCurrBean.currSpeed = Math.min(carCurrBean.currSpeed, distance);
                        StartTheCar(carCurrBean, time, carBeanQueues[i], i);
                        //return true;
                        break;
                    }
                }
            }
        }
    }

    private void StartTheCar(CarBean carCurrBean, int time, LinkedList<CarBean> carBeanQueue, int i) {


        if (carCurrBean.isStart || carCurrBean.isWaiting) throw new RuntimeException("程序内部逻辑错误");

        if (Constant.DEAD_LOCK_CHECK_FLAGE)
            ArrangeTheCarForEnd.currNoWaitingCarBeanList.add(carCurrBean.DeepCloneCarBean());
        carCurrBean.isStart = true;
        carCurrBean.channel = i;
        carCurrBean.isWaiting = false;
        //所有车辆个数+1
        //this.allTheCarNums++;
        //只设置非预置车辆
        if (carCurrBean.directionCar == 0)
            carCurrBean.startTime = time;

        carCurrBean.s1 = this.roadLength - carCurrBean.currSpeed;
        carCurrBean.currentRoadBean = this;
        //主要是优先车辆方向设置,初始化上路之前就设置过方向
        if (carCurrBean.directionCar == 1 || carCurrBean.isSetDirection) {
            carCurrBean.index++;
            carCurrBean.isSetDirection = false;
        } else carCurrBean.visitedEdges.add(this.roadId);
        carBeanQueue.add(carCurrBean);
        ArrangeTheCarForEnd.count++;
        //设置当前车辆开启
        ArrangeTheCarForEnd.allStartRunningCarBeanList.add(carCurrBean);
        //更新还未开启的车辆
        if (!ArrangeTheCarForEnd.allUnStartCarBeanList.remove(carCurrBean)) {
            throw new RuntimeException("移除失败!!!");
        }
    }


    /**
     * 计算相应的优先队列
     * <p>
     * 相同车道根据位置来判断优先级
     * 不同车道 首先根据优先级，如果优先级相同根据车道编号，
     */
    public void CalThePriorityQueue() {
        //先清空,再重新计算
        this.carBeanPriorityQueues.clear();
        //此条道路上所有的车辆
        LinkedList<CarBean>[] carBeanQueues = this.carBeanQueues;
        int len = carBeanQueues.length;
        int[] pointerIndex = new int[len];
        // 其实这可以不用(数组默认初始化为0),主要是为了方便阅读
        for (int i = 0; i < len; i++) {
            //保存每个车道队头元素位置
            pointerIndex[i] = 0;
        }
        //最多保存 len个元素
        PriorityQueue<CarBean> carBeans = new PriorityQueue<>(new MyCarPriorityComparator());
        for (int i = 0; i < len; i++) {//
            //某个队列为空，或者此辆车为终止状态
            if (pointerIndex[i] >= carBeanQueues[i].size() || !carBeanQueues[i].get(pointerIndex[i]).isWaiting)
                continue;
            carBeans.add(carBeanQueues[i].get(pointerIndex[i]));
        }
        //直到优先队列中元素为空
        while (!carBeans.isEmpty()) {
            CarBean poll = carBeans.poll();
            pointerIndex[poll.channel] += 1;
            carBeanPriorityQueues.add(poll);
            //某个队列队列中不存在车辆或者车辆处于终止状态
            if (pointerIndex[poll.channel] >= carBeanQueues[poll.channel].size() || !carBeanQueues[poll.channel].get(pointerIndex[poll.channel]).isWaiting)
                continue;
            //poll.channel车道上的车辆补充进去
            carBeans.add(carBeanQueues[poll.channel].get(pointerIndex[poll.channel]));
        }
    }

    /**
     * 更新优先队列,把终止状态的车辆移除
     */
    public void RemoveTheStopCarFromPriorityQueue() {

        while (!carBeanPriorityQueues.isEmpty() && !carBeanPriorityQueues.peek().isWaiting) {
            carBeanPriorityQueues.poll();
        }
    }
}
