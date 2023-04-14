package com.cherish.consumer.kafka;

import com.cherish.component.queue.impl.RedisDelayQueue;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.Order;
import com.cherish.service.order.OrderService;
import com.cherish.utils.JsonUtils;
import com.cherish.utils.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
public class OrderConfirmConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisDelayQueue redisDelayQueue;

    private Set<String> ACCEPT_EVENT_TYPES= new HashSet<>(Arrays.asList(OrderConstant.KafkaEvent.ORDER_APPROVE, OrderConstant.KafkaEvent.ORDER_DRIVER_ACCEPT));

    @KafkaListener(topics = {CommonConstant.KafkaTopic.ORDER_EVENT}, groupId = "order_confirm")
    public void handleMessage(ConsumerRecord<String, String> record,
                              @Header(CommonConstant.KafkaEvent.EVENT_TYPE_NAME) String eventType){
        if(!ACCEPT_EVENT_TYPES.contains(eventType)) return;
        Order order = JsonUtils.toObject(record.value(), Order.class);

        //1.MFF模式下Approve后开启商家状态流程
        //2.DFF模式下DriverAccept开启商家状态流程
        if(StringUtils.isEqual(eventType, OrderConstant.KafkaEvent.ORDER_APPROVE) &&!order.isDff() ||
                StringUtils.isEqual(eventType, OrderConstant.KafkaEvent.ORDER_DRIVER_ACCEPT) && order.isDff()){
            //1.1自动接单模式直接confirm订单
            if(order.isAutoConfirm()){
                orderService.confirmOrder(order);
            }
            //1.2手动接单模式，设置商家接单超时时间
            else{
                //设置商家在5～15分钟接单
                redisDelayQueue.push(OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_NAME, order.getId(),
                        (new Random().nextInt(11) + 5) * 60 * 1000);
                //设置商家在10分钟接单超时
                redisDelayQueue.push(OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_NAME, order.getId(),
                                     OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_VALUE);
            }
        }
    }
}
