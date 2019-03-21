package com.huawei.utils;

import com.huawei.beans.*;

import static com.huawei.utils.ReaddingTheData.crossBeanList;

public class BuildTheMapUtil {

    public static VexNode[] Graph = null;

    static {
        try {
            Class.forName(ReaddingTheData.class.getName(), true, Thread.currentThread().getContextClassLoader());
            Graph = BuildTheMapUtil.BuildTheVertecNodesMap();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        for (int e = ReaddingTheData.roadBeanList.size() - 1; e >= 0; e--) {
            RoadBean roadBean = ReaddingTheData.roadBeanList.get(e);
            int startCross = roadBean.startCross;
            int endCross = roadBean.endCross;
            int i = LocateVex(vertexNodes, startCross);
            int j = LocateVex(vertexNodes, endCross);
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


    private static int LocateVex(VexNode[] graph, int crossId) {

        for (int i = 0; i < graph.length; i++) {

            if (graph[i].crossId == crossId) {
                return i;
            }
        }

        throw new RuntimeException("内部数据错误");
    }



    public static void main(String... args) throws ClassNotFoundException {

    }
}
