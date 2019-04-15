package com.huawei;


import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length != 5) {
            //  logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }
        //logger.info("Start...");
        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];

        //logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " presetAnswerPath = " + presetAnswerPath + " and answerPath = " + answerPath);
        List<CrossBean> crossBeanList =
                ReaddingTheData.readCrossBeanList(crossPath);
        List<CarBean> carBeanList =
                ReaddingTheData.readCarBeanList(carPath, presetAnswerPath);
        List<RoadBean> roadBeanList =
                ReaddingTheData.readRoadBeanList(roadPath);
        Collections.sort(carBeanList);
        //logger.info("start read input files");
        ArrangeTheCarForEnd.ArrangeTheCar(crossBeanList, roadBeanList, carBeanList, answerPath);
        //调试参数的方法

        //logger.info("Start write output file");

        //logger.info("End...");
    }
}
