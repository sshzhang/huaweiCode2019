package com.huawei.Main;

import com.huawei.beans.CarBean;
import com.huawei.beans.CrossBean;
import com.huawei.beans.RoadBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 构建图
 */
public class BuildTheMap {


    public static List<CrossBean> crossBeanList = new ArrayList<>();
    public static List<RoadBean> roadBeanList = new ArrayList<>();
    public static List<CarBean> carBeanList = new ArrayList<>();

    static {
        readCrossBeanList();
        readRoadBeanList();
        readCarBeanList();
        Collections.sort(carBeanList);
    }

    public static List<CrossBean> readCrossBeanList() {
        String str = null;
        try (//读取其中的road.txt文件
             InputStream resourceAsStream = BuildTheMap.class.getClassLoader().getResourceAsStream("Cross.txt");
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
             InputStream resourceAsStream = BuildTheMap.class.getClassLoader().getResourceAsStream("Road.txt");
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
             InputStream resourceAsStream = BuildTheMap.class.getClassLoader().getResourceAsStream("Car.txt");
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


    //邻接表
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

    public static void main(String... args) {

        System.out.println(carBeanList.size());
        VertexNode[] vertexNodes = BuildTheVertecNodesMap();
        System.out.println(vertexNodes);

        int[] dp = new int[vertexNodes.length];
        ShortestPath_DJSTR(vertexNodes, 1, dp);
        System.out.println(dp);

    }

    /**
     * 对于每辆车判断可能的路径
     */
    private static void ArrangeTheCar(VertexNode verTexNodes[]) {
        //最短路径算法 求所有点之间的最短路径

        //当前调度时刻
        int time = 0;

        while (isAllFinish()) {//依然有车量在运行
            time++;
            for (int k = 0; k < carBeanList.size(); k++) {
                CarBean carBean = carBeanList.get(k);
                //车没有调度完成
                if (!carBean.isFinish) {
                    //车在运行
                    if (carBean.isStart) {

                    } else {
                        //开始调度
                        if (carBean.startTime <= time) {
                            //计算此时最佳的路线
                            int[] dp = new int[verTexNodes.length];
                            int[][] path = ShortestPath_DJSTR(verTexNodes, carBean.startBean, dp);


                        }
                    }
                }
            }
        }
    }


    //verTexId表示某个节点的crossId  verTexI　此函数需要数组的id和 crossId对应
    public static int[][] ShortestPath_DJSTR(VertexNode verTexNodes[], int verTexId, int d[]) {

//        int d[] = new int[verTexNodes.length];
        int path[][] = new int[verTexNodes.length][verTexNodes.length];

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

            if (min == Integer.MAX_VALUE) return path;

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

        return path;

    }


    private static boolean isAllFinish() {

        for (int i = 0; i < carBeanList.size(); i++) {
            if (!carBeanList.get(i).isFinish) {

                return false;
            }
        }

        return true;
    }


    //调度所有点
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
