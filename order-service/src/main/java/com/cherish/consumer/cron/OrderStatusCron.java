package com.cherish.consumer.cron;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.dao.OrderDao;
import com.cherish.entity.order.CancelOrderReq;
import com.cherish.entity.order.Order;
import com.cherish.entity.rpc.Response;
import com.cherish.service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OrderStatusCron {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    private final int batchNum = 100;

    Logger logger = LoggerFactory.getLogger(OrderStatusCron.class);

    /**
     * 定时任务补偿处理取消超时没有支付的订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void cancelPayExpiredOrder(){
        //1. 遍历每个shard
        for(int shardId=0 ; shardId< CommonConstant.DbShardNum.ORDER; shardId++){
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                LambdaQueryWrapper queryWrapper = Wrappers.<Order>lambdaQuery()
                                                         .eq(Order::getStatus, OrderConstant.OrderStatus.CREATED)
                                                         .lt(Order::getStatusUpdateTime, System.currentTimeMillis() - OrderConstant.DelayTask.ORDER_PAY_EXPIRED_VALUE)
                                                         .gt(Order::getId, lastId);
                List<Order> orders = orderDao.getOrders(queryWrapper, shardId, batchNum);
                if (orders.size() < batchNum) flag = false;
                if (orders.isEmpty()) break;
                lastId =  orders.get(orders.size()-1).getId();
                for (Order order : orders) {
                    CancelOrderReq cancelOrderReq = CancelOrderReq.builder()
                            .orderId(order.getId())
                            .cancelReason(OrderConstant.CancelReason.PAID_TIMEOUT)
                            .cancelSource(OrderConstant.CancelSource.SYSTEM)
                            .build();
                    Response<Void> cancelResponse = orderService.cancelOrder(cancelOrderReq);
                    if (!cancelResponse.isSuccess())
                        logger.error("定时任务取消超时未支付订单失败,orderId:{},错误码:{}", order.getId(), cancelResponse.getCode());
                    else logger.info("定时任务取消超时未支付订单成功,orderId:{}", order.getId());
                }
            }
        }
    }

    /**
     * 定时任务补偿kafka未approve订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void approveOrder(){
        for(int shardId=0; shardId< CommonConstant.DbShardNum.ORDER; shardId++){
            long lastId = 0;
            boolean flag = true;
            while (flag){
                LambdaQueryWrapper queryWrapper = Wrappers.<Order>lambdaQuery()
                                                          .eq(Order::getStatus, OrderConstant.OrderStatus.PAID)
                                                          //处理一分钟内还没有approve的订单，如果不设置这个容易定时器和kafka进行重复消费
                                                          .lt(Order::getStatusUpdateTime, System.currentTimeMillis() - 60 * 1000)
                                                          .gt(Order::getId, lastId);
                List<Order> orders = orderDao.getOrders(queryWrapper, shardId, batchNum);
                if (orders.size() < batchNum) flag = false;
                if (orders.isEmpty()) break;
                lastId =  orders.get(orders.size()-1).getId();
                for (Order order : orders) {
                    Response<Void> approveResponse = orderService.approveOrder(order);
                    if(!approveResponse.isSuccess()) logger.error("定时器处理自动approve订单失败,orderId:{},错误码:{}", order.getId(), approveResponse.getCode());
                    else logger.info("定时器处理系统超时自动approve订单成功,orderId:{}", order.getId());
                }
            }
        }
    }

    /**
     * 定时任务补偿处理超时没有商家接单的订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void dealConfirmExpiredOrder(){
        for(int shardId=0; shardId< CommonConstant.DbShardNum.ORDER; shardId++) {
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                LambdaQueryWrapper queryWrapper = Wrappers.<Order>lambdaQuery()
                                                         .eq(Order::getMerchantConfirmTime, 0)
                                                         .ne(Order::getStatus, OrderConstant.OrderStatus.CANCELED)
                                                         .lt(Order::getMerchantDeadline, System.currentTimeMillis())
                                                         .eq(Order::getIsMerchantVisible, true)
                                                         .gt(Order::getId, lastId);
                List<Order> orders = orderDao.getOrders(queryWrapper, shardId, batchNum);
                if (orders.size() < batchNum) flag = false;
                if (orders.isEmpty()) break;
                lastId =  orders.get(orders.size()-1).getId();
                for (Order order : orders) {
                    Response<Void> confirmExpiredResponse = orderService.merchantConfirmExpired(order);
                    if (!confirmExpiredResponse.isSuccess())  logger.error("定时任务处理商家超时未接单失败,orderId:{},错误码:{}", order.getId(), confirmExpiredResponse.getCode());
                    else logger.info("定时任务处理商家超时未接单成功,orderId:{}", order.getId());
                }
            }
        }
    }

    /**
     * 定时任务将delivered一个小时的订单扭转成completed状态
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void dealDeliveredOrder(){
        for(int shardId=0; shardId< CommonConstant.DbShardNum.ORDER; shardId++) {
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                LambdaQueryWrapper queryWrapper = Wrappers.<Order>lambdaQuery()
                                                          .eq(Order::getStatus, OrderConstant.OrderStatus.DELIVERED)
                                                          .lt(Order::getStatusUpdateTime, System.currentTimeMillis() - 60 * 60 * 1000)
                                                          .gt(Order::getId, lastId);
                List<Order> orders = orderDao.getOrders(queryWrapper, shardId, batchNum);
                if (orders.size() < batchNum) flag = false;
                if (orders.isEmpty()) break;
                lastId =  orders.get(orders.size()-1).getId();
                for (Order order : orders) {
                    Response<Void> completeResponse = orderService.completeOrder(order);
                    if (!completeResponse.isSuccess())  logger.error("定时任务扭转订单状态completed失败,orderId:{},错误码:{}", order.getId(), completeResponse.getCode());
                    else logger.info("定时任务扭转订单状态completed成功,orderId:{}", order.getId());
                }
            }
        }
    }
}
