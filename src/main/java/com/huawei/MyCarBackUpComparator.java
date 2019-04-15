package com.huawei;

import java.util.Comparator;

/**
 * 回溯的时候对备份车辆排序,方便插入到道路上
 */
public class MyCarBackUpComparator implements Comparator<CarBean> {


    /**
     * 道路id升序, 车道号升序, 距离升序
     *
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(CarBean o1, CarBean o2) {

        int orderId = ((o1.currentRoadBean != null ? o1.currentRoadBean.roadId : -1) - (o2.currentRoadBean != null ? o2.currentRoadBean.roadId : -1));
        int orderChannel = o1.channel - o2.channel;
        int orderS1 = o1.s1 - o2.s1;

        return orderId != 0 ? orderId : (orderChannel != 0 ? orderChannel : orderS1);
    }
}
