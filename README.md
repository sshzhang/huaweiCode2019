# huaweiCode2019
华为软挑2019

杭厦赛区   队伍名--文泽路最亮的车   初赛第九，复赛第八.

车辆调度思路：

     其实我们本质上就是根据调度规则来调度车辆， 主要算法就是最短路径，但我们在设置权重的时候考虑了拥堵量和速度，
     同时当一条道路拥堵无法上车时，我们直接设置此条边不可见。每次判断一辆车的方向时，我们都更新全图所有道路的权重。
     然后计算最短路径，最短路径也分为两种，一种相对最短路径(考虑拥堵情况，到达目的点的路径不存在)；
     另一种绝对最短路径，只有当路径不存在时 才调用设置车辆的方向。

死锁回溯解决：

     为了解决死锁，我们设置死锁回溯，每次死锁的道路权重都会加上一个惩罚项，从而回溯的时候不会走原来的道路。 
     我们在Constant文件中能够设置启用死锁回溯，和每次回溯多少个时间片以及道路的惩罚权重是多少。当然你设置的回溯时间片打，
     回溯所消耗的时间越大。
        
   
 关于发车策略， 这一块主要是根据未来的优先车和预制车的数量来控制。动态调整在运行额普通车辆的个数。
 
 复赛：现场赛的时候，我们就是通过设置死锁回溯跑出结果。缺点是太慢，时间来不及，还有如果回溯次数设置太大，可能超时，不过我没遇到过。
 
 
