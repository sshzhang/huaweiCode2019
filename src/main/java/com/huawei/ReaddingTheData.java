package com.huawei;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaddingTheData {


    public static int priorityEarlyPlanTime = Integer.MAX_VALUE;

    public static Map<Integer, Integer> priorityStartCross = new HashMap<>();

    public static Map<Integer, Integer> priorityEndCross = new HashMap<>();


    public static Map<Integer, Integer> StartCross = new HashMap<>();

    public static Map<Integer, Integer> EndCross = new HashMap<>();

    protected static List<CrossBean> readCrossBeanList(String crossfile) {

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

    protected static List<RoadBean> readRoadBeanList(String roadFile) {
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

    protected static List<CarBean> readCarBeanList(String carFile, String presetFile) {
        List<CarBean> carBeanList = new ArrayList<>();
        String str = null;
        try (//读取其中的road.txt文件
             //InputStream resourceAsStream = ReaddingTheData.class.getClassLoader().getResourceAsStream("car.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(carFile)))) {
            Pattern compile = Pattern.compile("\\((?<carId>[0-9]+),[ ]+(?<startCrossId>[0-9]+),[ ]+(?<endCrossId>[0-9]+),[ ]+(?<maxSpeed>[0-9]+),[ ]+(?<time>[0-9]+),[ ]+(?<priority>[0-1]),[ ]+(?<preset>[0-1])\\)");
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
                        int priority = Integer.parseInt(matcher.group("priority"));
                        int preset = Integer.parseInt(matcher.group("preset"));
                        priorityEarlyPlanTime = priority == 1 ? (priorityEarlyPlanTime > time ? time : priorityEarlyPlanTime) : priorityEarlyPlanTime;
                        ArrangeTheCarForEnd.priorityCarNum = priority == 1 ? ArrangeTheCarForEnd.priorityCarNum + 1 : ArrangeTheCarForEnd.priorityCarNum;
                        ArrangeTheCarForEnd.allCarMaxEarlyTime = time > ArrangeTheCarForEnd.allCarMaxEarlyTime ? ArrangeTheCarForEnd.allCarMaxEarlyTime : time;
                        ArrangeTheCarForEnd.allCarMinLateTime = time > ArrangeTheCarForEnd.allCarMinLateTime ? time : ArrangeTheCarForEnd.allCarMinLateTime;
                        ArrangeTheCarForEnd.allCarMaxSpeed = maxSpeed > ArrangeTheCarForEnd.allCarMaxSpeed ? maxSpeed : ArrangeTheCarForEnd.allCarMaxSpeed;
                        ArrangeTheCarForEnd.allCarMinSpeed = maxSpeed > ArrangeTheCarForEnd.allCarMinSpeed ? ArrangeTheCarForEnd.allCarMinSpeed : maxSpeed;
                        ArrangeTheCarForEnd.priorityCarEarlyTime = priority == 1 ? (time > ArrangeTheCarForEnd.priorityCarEarlyTime ? ArrangeTheCarForEnd.priorityCarEarlyTime : time) : ArrangeTheCarForEnd.priorityCarEarlyTime;
                        ArrangeTheCarForEnd.priorityCarLateTime = priority == 1 ? (time > ArrangeTheCarForEnd.priorityCarLateTime ? time : ArrangeTheCarForEnd.priorityCarLateTime) : ArrangeTheCarForEnd.priorityCarLateTime;
                        ArrangeTheCarForEnd.priorityCarMaxSpeed = priority == 1 ? (maxSpeed > ArrangeTheCarForEnd.priorityCarMaxSpeed ? maxSpeed : ArrangeTheCarForEnd.priorityCarMaxSpeed) : ArrangeTheCarForEnd.priorityCarMaxSpeed;
                        ArrangeTheCarForEnd.priorityCarMinSpeed = priority == 1 ? (maxSpeed > ArrangeTheCarForEnd.priorityCarMinSpeed ? ArrangeTheCarForEnd.priorityCarMinSpeed : maxSpeed) : ArrangeTheCarForEnd.priorityCarMinSpeed;

                        if (priority == 1) {
                            priorityStartCross.put(startCrossId, priorityStartCross.getOrDefault(startCrossId, 0) + 1);
                            priorityEndCross.put(endCrossId, priorityEndCross.getOrDefault(startCrossId, 0) + 1);
                        }

                        StartCross.put(startCrossId, StartCross.getOrDefault(startCrossId, 0) + 1);
                        EndCross.put(endCrossId, EndCross.getOrDefault(startCrossId, 0) + 1);

                        carBeanList.add(new CarBean(carId, startCrossId, endCrossId, maxSpeed, time, priority, preset));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //读取预置路线
        readPresetCarBeanList(carBeanList, presetFile);
        return carBeanList;
    }

    /**
     * 读取预置路线
     *
     * @param carBeanList
     * @param presetFile
     */
    public static void readPresetCarBeanList(List<CarBean> carBeanList, String presetFile) {

        String str = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(presetFile)))) {
            Pattern compile = Pattern.compile("\\((?<carId>[0-9]+),[ ]?(?<time>[0-9]+),[ ]?(?<roadId1>.*)\\)");
            while ((str = bufferedReader.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                } else {
                    Matcher matcher = compile.matcher(str);
                    if (matcher.find()) {
                        int carId = Integer.parseInt(matcher.group("carId"));
                        int time = Integer.parseInt(matcher.group("time"));
                        String roadIds[] = matcher.group("roadId1").split(", ");
                        CarBean carBean = FindTheCarByCarId(carBeanList, carId);
                        for (String roadId : roadIds) {
                            carBean.visitedEdges.add(Integer.parseInt(roadId));
                        }
                        carBean.startTime = time;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 根据车的id找出车对应的信息
     *
     * @param carBeanList
     * @param carId
     * @return
     */
    public static CarBean FindTheCarByCarId(List<CarBean> carBeanList, int carId) {
        for (CarBean carBean : carBeanList) {
            if (carBean.carId == carId) {
                return carBean;
            }
        }
        throw new RuntimeException("presetAnswer.txt数据格式异常");
    }

/*    public static void main(String... args) {

        String str = "(21701,1,5175, 6484, 6549, 6225, 5489, 6081, 6970, 5090, 5026, 6875, 6997, 5759)";

        Pattern compile = Pattern.compile("\\((?<carId>[0-9]+),(?<time>[0-9]+),(?<roadId1>.*)\\)");

        Matcher matcher = compile.matcher(str);

        if (matcher.find()) {
            int carId = Integer.parseInt(matcher.group("carId"));
            int time = Integer.parseInt(matcher.group("time"));
            String roadInfo = matcher.group("roadId1");
            System.out.println(roadInfo);

        }
    }*/
}



