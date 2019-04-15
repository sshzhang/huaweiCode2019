package com.huawei;

public class CrossBean {

    //交叉路口的id
    public int crossId;

    public int upRoadId;

    public int rightRoadId;

    public int downRoadId;

    public int leftRoadId;

    public CrossBean(int crossId, int upRoadId, int rightRoadId, int downRoadId, int leftRoadId) {

        this.crossId = crossId;
        this.upRoadId = upRoadId;
        this.rightRoadId = rightRoadId;
        this.downRoadId = downRoadId;
        this.leftRoadId = leftRoadId;
    }

/*    public static void main(String... args) {

        List<String> strings = new ArrayList<>();

        strings.add("1");
        strings.add("2");


        List<String> sm = new ArrayList<>(strings);
        sm.add("3");

        System.out.println(strings.size());


    }*/
}
