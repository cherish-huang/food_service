package com.cherish.consumer.cron;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.DeliveryConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.dao.OrderDao;
import com.cherish.entity.delivery.CreateDeliveryReq;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.delivery.QueryDeliveryOrderByOrderIdReq;
import com.cherish.entity.order.CancelOrderReq;
import com.cherish.entity.order.DriverAcceptReq;
import com.cherish.entity.order.DriverDeliverOrderReq;
import com.cherish.entity.order.Order;
import com.cherish.entity.rpc.Response;
import com.cherish.service.delivery.DeliveryService;
import com.cherish.service.order.OrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MakeupDeliveryCron {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    @DubboReference(check = false)
    private DeliveryService deliveryService;

    private final int batchNum = 100;

    Logger logger = LoggerFactory.getLogger(MakeupDeliveryCron.class);

    /**
     * 定时任务补偿处理订单没有创建运单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void makeupCreateAcceptDeliveryOrder() {
        //1. 遍历每个shard
        for (int shardId = 0; shardId < CommonConstant.DbShardNum.ORDER; shardId++) {
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                //2.遍历db将满足创建运单条件的订单取出来
                //1).下单一个小时以内
                //2).状态在Approve或者merchantConfirm状态
                //3).上一次状态扭转30s以外
                LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                        .in(Order::getStatus, Arrays.asList(OrderConstant.OrderStatus.APPROVED, OrderConstant.OrderStatus.MERCHANT_CONFIRMED))
                        .gt(Order::getSubmitTime, System.currentTimeMillis() - 60 * 60 * 1000)
                        .lt(Order::getStatusUpdateTime, System.currentTimeMillis() - 30 * 1000)
                        .gt(Order::getId, lastId);
                List<Order> orders = orderDao.getOrders(queryWrapper, shardId, batchNum);
                if (orders.size() < batchNum) flag = false;
                if (orders.isEmpty()) break;
                lastId = orders.get(orders.size()-1).getId();
                Map<Long, Order> orderMap = orders.stream().filter(this::filterNeedCreateDelivery).collect(Collectors.toMap(Order::getId, Function.identity()));
                if (orderMap.isEmpty()) continue;
                //3.批量调用运单获取订单对应的运单
                Response<List<DeliveryOrder>> deliveriesResponse = deliveryService.queryDeliveryOrders(QueryDeliveryOrderByOrderIdReq.builder().ids(new ArrayList<>(orderMap.keySet())).build());
                if (!deliveriesResponse.isSuccess()) continue;
                Map<Long, DeliveryOrder> deliveryOrderMap = deliveriesResponse.getData().stream().collect(Collectors.toMap(DeliveryOrder::getOrderId, Function.identity()));
                for (Long orderId : orderMap.keySet()) {
                    DeliveryOrder deliveryOrder = deliveryOrderMap.get(orderId);
                    //4.运单还没有创建 需要创建运单
                    if (!deliveryOrderMap.containsKey(orderId)) {
                        Response<Long> response = deliveryService.createDelivery(buildDeliveryReq(orderMap.get(orderId)));
                        if(response.isSuccess()) logger.info("makeupCreateAcceptDeliveryOrder 补偿createDelivery成功,id={}", orderId);
                        else logger.warn("makeupCreateAcceptDeliveryOrder 补偿createDelivery失败,id={},code={}", orderId, response.getCode());
                    }
                    //5.如果运单的状态骑手已经接单则补偿kafka骑手接单消息失败
                    else if(deliveryOrder.getStatus() >= DeliveryConstant.DeliveryOrderStatus.CONFIRMED
                            &&deliveryOrder.getStatus() <= DeliveryConstant.DeliveryOrderStatus.COMPLETED){
                        Response<Void>  response = orderService.driverAccept(DriverAcceptReq.builder().orderId(orderId).driverAcceptTime(deliveryOrder.getAssignTime()).build());
                        if(response.isSuccess()) logger.info("makeupCreateAcceptDeliveryOrder 补偿acceptDelivery成功,id={}", orderId);
                        else logger.warn("makeupCreateAcceptDeliveryOrder 补偿acceptDelivery失败,id={},code={}", orderId, response.getCode());
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void makeupDeliveryOrder() {
        //1. 遍历每个shard
        for (int shardId = 0; shardId < CommonConstant.DbShardNum.ORDER; shardId++) {
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                //2.遍历db将满足创建运单条件的订单取出来
                //1).状态在driverAccept,driverArrive,driverPickup,merchantConfirm(dff模式)
                //2).上一次状态扭转1小时以外
                LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                        .in(Order::getStatus, Arrays.asList(OrderConstant.OrderStatus.DRIVER_ACCEPTED, OrderConstant.OrderStatus.DRIVER_ARRIVED,
                                                            OrderConstant.OrderStatus.DRIVER_PICKUPED, OrderConstant.OrderStatus.MERCHANT_CONFIRMED))
                        .lt(Order::getStatusUpdateTime, System.currentTimeMillis() - 60 * 60 * 1000)
                        .gt(Order::getId, lastId);
                List<Order> orders = orderDao.getOrders(queryWrapper, shardId, batchNum);
                if (orders.size() < batchNum) flag = false;
                if (orders.isEmpty()) break;
                lastId = orders.get(orders.size() - 1).getId();
                Map<Long, Order> orderMap = orders.stream().filter(this::filterNeedMakeupDelivery).collect(Collectors.toMap(Order::getId, Function.identity()));
                if (orderMap.isEmpty()) continue;
                //3.批量调用运单获取订单对应的运单
                Response<List<DeliveryOrder>> deliveriesResponse = deliveryService.queryDeliveryOrders(QueryDeliveryOrderByOrderIdReq.builder().ids(new ArrayList<>(orderMap.keySet())).build());
                if (!deliveriesResponse.isSuccess()) continue;
                Map<Long, DeliveryOrder> deliveryOrderMap = deliveriesResponse.getData().stream().collect(Collectors.toMap(DeliveryOrder::getOrderId, Function.identity()));
                for (Long orderId : deliveryOrderMap.keySet()) {
                    switch (deliveryOrderMap.get(orderId).getStatus()){
                        //4.1 补偿已经deliver的订单
                        case DeliveryConstant.DeliveryOrderStatus.COMPLETED:
                            Response<Void> deliverResponse = orderService.driverDeliver(DriverDeliverOrderReq.builder().orderId(orderId).build());
                            if(!deliverResponse.isSuccess()) logger.warn("makeupDeliveryOrder补偿deliver状态失败,id={},code={}", orderId, deliverResponse.getCode());
                            else logger.info("makeupDeliveryOrder补偿deliver状态成功,id={}", orderId);
                            break;
                        //4.2 补偿已经cancel的订单
                        case DeliveryConstant.DeliveryOrderStatus.CANCELED:
                            CancelOrderReq cancelOrderReq = CancelOrderReq.builder().orderId(orderId)
                                                                                    .cancelSource(OrderConstant.CancelSource.DRIVER)
                                                                                    .cancelReason(OrderConstant.CancelReason.DELIVERY_CANCELED)
                                                                                    .build();
                            Response<Void> cancelResponse = orderService.cancelOrder(cancelOrderReq);
                            if(!cancelResponse.isSuccess()) logger.warn("makeupDeliveryOrder补偿cancel状态失败,id={},code={}", orderId, cancelResponse.getCode());
                            else logger.info("makeupDeliveryOrder补偿cancel状态成功,id={}", orderId);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private boolean filterNeedMakeupDelivery(Order order){
        //1.dff模式下处于merchant_confirm需要补偿
        if(order.getStatus() == OrderConstant.OrderStatus.MERCHANT_CONFIRMED) return order.isDff();
        return true;
    }

    private boolean filterNeedCreateDelivery(Order order){
        //1.dff模式下处于approve状态的订单并且在approve超过10s的订单才需要补偿订单(10s为了和api同步调用错开)
        if(order.isDff() && order.getStatus() == OrderConstant.OrderStatus.APPROVED) return true;
        //
        if(!order.isDff() && order.getStatus() == OrderConstant.OrderStatus.MERCHANT_CONFIRMED) return true;

        return false;
    }

    private CreateDeliveryReq buildDeliveryReq(Order order){
        return CreateDeliveryReq.builder()
                .orderId(order.getId())
                .storeId(order.getStoreId())
                .deliveryName(order.getDeliveryName())
                .deliveryPhone(order.getDeliveryPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryLatitude(order.getDeliveryLatitude())
                .deliveryLongitude(order.getDeliveryLongitude())
                .isDff(order.isDff())
                .build();
    }
}
