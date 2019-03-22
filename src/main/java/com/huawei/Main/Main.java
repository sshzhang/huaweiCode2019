package com.huawei.Main;

//import org.apache.log4j.Logger;

import com.huawei.beans.CarBean;
import com.huawei.beans.CrossBean;
import com.huawei.beans.RoadBean;
import com.huawei.utils.ArrangeTheCarForEnd;
import com.huawei.utils.ReaddingTheData;

import java.util.Collections;
import java.util.List;

public class Main {
//    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 4) {
//            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }

//        logger.info("Start...");

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String answerPath = args[3];
//        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " and answerPath = " + answerPath);

        List<CrossBean> crossBeanList =
                ReaddingTheData.readCrossBeanList(crossPath);

        List<CarBean> carBeanList =
                ReaddingTheData.readCarBeanList(carPath, crossBeanList.size());

        List<RoadBean> roadBeanList =
                ReaddingTheData.readRoadBeanList(roadPath);


        Collections.sort(carBeanList);
//        logger.info("start read input files");

        ArrangeTheCarForEnd.ArrangeTheCar(crossBeanList, roadBeanList, carBeanList, answerPath);


//        logger.info("Start write output file");

//        logger.info("End...");
    }
}