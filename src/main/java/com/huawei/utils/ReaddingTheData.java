package com.huawei.utils;

import com.huawei.beans.CarBean;
import com.huawei.beans.CrossBean;
import com.huawei.beans.RoadBean;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaddingTheData {

    //private static List<CrossBean> crossBeanList = new ArrayList<>();
    //private static List<RoadBean> roadBeanList = new ArrayList<>();
    //private static List<CarBean> carBeanList = new ArrayList<>();



    public static List<CrossBean> readCrossBeanList(String crossfile) {

        List<CrossBean> crossBeanList = new ArrayList<>();
        String str = null;
        try (//读取其中的road.txt文件
             //InputStream resourceAsStream = ReaddingTheData.class.getClassLoader().getResourceAsStream("cross.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(crossfile)))) {
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

    public static List<RoadBean> readRoadBeanList(String roadFile) {
        List<RoadBean> roadBeanList = new ArrayList<>();
        String str = null;
        try (//读取其中的road.txt文件
             //InputStream resourceAsStream = ReaddingTheData.class.getClassLoader().getResourceAsStream("road.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(roadFile)))) {
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
                        roadBeanList.add(new RoadBean(roadId, roadLength, speedLimit, roadNums, startCross, endCross, isBothWay, true));
                        if (isBothWay == 1) {
                            roadBeanList.add(new RoadBean(roadId, roadLength, speedLimit, roadNums, endCross, startCross, isBothWay, true));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return roadBeanList;
    }

    public static List<CarBean> readCarBeanList(String carFile, int size) {
        List<CarBean> carBeanList = new ArrayList<>();

        String str = null;
        try (//读取其中的road.txt文件
             //InputStream resourceAsStream = ReaddingTheData.class.getClassLoader().getResourceAsStream("car.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(carFile)))) {
            Pattern compile = Pattern.compile("\\((?<carId>[0-9]+),[ ]+(?<startCrossId>[0-9]+),[ ]+(?<endCrossId>[0-9]+),[ ]+(?<maxSpeed>[0-9]+),[ ]+(?<time>[0-9]+)\\)");
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
                        carBeanList.add(new CarBean(carId, startCrossId, endCrossId, maxSpeed, time, false, size));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return carBeanList;
    }

}
