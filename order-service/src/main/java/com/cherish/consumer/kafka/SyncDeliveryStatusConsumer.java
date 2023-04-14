package com.cherish.consumer.kafka;

import com.cherish.constant.CommonConstant;
import com.cherish.constant.DeliveryConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.order.*;
import com.cherish.entity.rpc.Response;
import com.cherish.service.order.OrderService;
import com.cherish.utils.JsonUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class SyncDeliveryStatusConsumer {

    @Autowired
    private OrderService orderService;

    private Set<String> ACCEPT_EVENT_TYPES= new HashSet<>(Arrays.asList(DeliveryConstant.KafkaEvent.DELIVERY_ACCEPT,
                                                                        DeliveryConstant.KafkaEvent.DELIVERY_ARRIVE,
                                                                        DeliveryConstant.KafkaEvent.DELIVERY_PICKUP,
                                                                        DeliveryConstant.KafkaEvent.DELIVERY_COMPLETE));
    Logger logger = LoggerFactory.getLogger(SyncDeliveryStatusConsumer.class);

    @KafkaListener(topics = {CommonConstant.KafkaTopic.DELIVERY_EVENT}, groupId = "order_sync_delivery_status")
    public void handleMessage(ConsumerRecord<String, String> record,
                              @Header(CommonConstant.KafkaEvent.EVENT_TYPE_NAME) String eventType){
        if(!ACCEPT_EVENT_TYPES.contains(eventType)) return;
        DeliveryOrder deliveryOrder = JsonUtils.toObject(record.value(), DeliveryOrder.class);
        switch (eventType){
            //1.骑手接单
            case DeliveryConstant.KafkaEvent.DELIVERY_ACCEPT:
                Response<Void> acceptResponse = orderService.driverAccept(DriverAcceptReq.builder().orderId(deliveryOrder.getOrderId()).build());
                if(acceptResponse.isSuccess()) logger.info("SyncDeliveryStatusConsumer同步delivery.accept事件成功,id={}", deliveryOrder.getOrderId());
                else logger.warn("SyncDeliveryStatusConsumer同步delivery.accept事件失败,id={},code={}", deliveryOrder.getOrderId(), acceptResponse.getCode());
                return;
            //2.骑手到店
            case DeliveryConstant.KafkaEvent.DELIVERY_ARRIVE:
                //2.1如果订单骑手状态没有accept,则需要补偿accept
                Response<Void> arriveResponse = orderService.driverArrive(DriverArriveReq.builder().orderId(deliveryOrder.getOrderId())
                                                                                                    .driverAcceptTime(deliveryOrder.getAssignTime()).build());
                if(arriveResponse.isSuccess()) logger.info("SyncDeliveryStatusConsumer同步delivery.arrive事件成功,id={}", deliveryOrder.getOrderId());
                else logger.warn("SyncDeliveryStatusConsumer同步delivery.arrive事件失败,id={},code={}", deliveryOrder.getOrderId(), arriveResponse.getCode());
                return;
            //3.骑手取餐
            case DeliveryConstant.KafkaEvent.DELIVERY_PICKUP:
                Response<Void> pickupResponse = orderService.driverPickup(DriverPickupReq.builder().orderId(deliveryOrder.getOrderId())
                                                            .driverAcceptTime(deliveryOrder.getAssignTime()).driverArriveTime(deliveryOrder.getArriveTime()).build());
                if(pickupResponse.isSuccess()) logger.info("SyncDeliveryStatusConsumer同步delivery.pickup事件成功,id={}", deliveryOrder.getOrderId());
                else logger.warn("SyncDeliveryStatusConsumer同步delivery.pickup事件失败,id={},code={}", deliveryOrder.getOrderId(), pickupResponse.getCode());
                return;
            //4.配送完成
            case DeliveryConstant.KafkaEvent.DELIVERY_COMPLETE:
                Response<Void> completeResponse = orderService.driverDeliver(DriverDeliverOrderReq.builder().orderId(deliveryOrder.getOrderId())
                                                              .driverAcceptTime(deliveryOrder.getAssignTime()).driverArriveTime(deliveryOrder.getArriveTime())
                                                              .driverPickupTime(deliveryOrder.getPickupTime()).build());
                if(completeResponse.isSuccess()) logger.info("SyncDeliveryStatusConsumer同步delivery.complete事件成功,id={}", deliveryOrder.getOrderId());
                else logger.warn("SyncDeliveryStatusConsumer同步delivery.complete事件失败,id={},code={}", deliveryOrder.getOrderId(), completeResponse.getCode());
                return;
            //5.运单取消
            case DeliveryConstant.KafkaEvent.DELIVERY_CANCEL:
                Response<Void> cancelResponse = orderService.cancelOrder(CancelOrderReq.builder().orderId(deliveryOrder.getOrderId())
                                                                        .cancelSource(OrderConstant.CancelSource.DRIVER)
                                                                        .cancelReason(OrderConstant.CancelReason.DELIVERY_CANCELED).build());
                if(cancelResponse.isSuccess()) logger.info("SyncDeliveryStatusConsumer同步delivery.cancel事件成功,id={}", deliveryOrder.getOrderId());
                else logger.warn("SyncDeliveryStatusConsumer同步delivery.cancel事件失败,id={},code={}", deliveryOrder.getOrderId(), cancelResponse.getCode());
        }
    }
}
