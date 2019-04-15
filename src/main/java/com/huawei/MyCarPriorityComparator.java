package com.huawei;

import java.util.Comparator;

/**
 * 构造优先队列时的排序器
 * 按照优先级降序,车道号升序
 */
public class MyCarPriorityComparator implements Comparator<CarBean> {

    @Override
    public int compare(CarBean o1, CarBean o2) {

        //按照优先级降序,车道号升序
        return o1.priorityCar - o2.priorityCar != 0 ? o2.priorityCar - o1.priorityCar : (o1.s1-o2.s1!=0?o1.s1-o2.s1:o1.channel - o2.channel);
    }


 /*   public static void main(String... args) {


        PriorityQueue<CarBean> carBeans = new PriorityQueue<>(new MyCarPriorityComparator());

        carBeans.add(new CarBean(1, 1, 2));

        carBeans.add(new CarBean(2, 1, 1));

        carBeans.add(new CarBean(3, 0, 1));



        System.out.println(carBeans.poll().carId);


    }*/


}
