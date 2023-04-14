package com.cherish.consumer.kafka;

import com.cherish.component.queue.impl.RedisDelayQueue;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.DeliveryConstant;
import com.cherish.entity.delivery.DeliveryOrder;
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
import java.util.Random;
import java.util.Set;

@Component
public class FollowDeliveryStatus {

    @Autowired
    private RedisDelayQueue redisDelayQueue;

    private Set<String> ACCEPT_EVENT_TYPES= new HashSet<>(Arrays.asList(DeliveryConstant.KafkaEvent.DELIVERY_ACCEPT,
                                                                        DeliveryConstant.KafkaEvent.DELIVERY_ARRIVE,
                                                                        DeliveryConstant.KafkaEvent.DELIVERY_PICKUP));

    Logger logger = LoggerFactory.getLogger(FollowDeliveryStatus.class);

    @KafkaListener(topics = {CommonConstant.KafkaTopic.DELIVERY_EVENT}, groupId = "delivery_follow_status")
    public void handleMessage(ConsumerRecord<String, String> record,
                              @Header(CommonConstant.KafkaEvent.EVENT_TYPE_NAME) String eventType){
        if(!ACCEPT_EVENT_TYPES.contains(eventType)) return;
        DeliveryOrder deliveryOrder = JsonUtils.toObject(record.value(), DeliveryOrder.class);
        switch (eventType){
            case DeliveryConstant.KafkaEvent.DELIVERY_ACCEPT:
                // 10~30到店
                int delayTime = (new Random().nextInt(21) + 10) * 60 * 1000;
                redisDelayQueue.push(DeliveryConstant.DelayTask.delivery_arrive_task, deliveryOrder.getId(), delayTime);
                logger.info("FollowDeliveryStatus设置到店任务成功,id={},orderId={},delayTime={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), delayTime);
                break;
            case DeliveryConstant.KafkaEvent.DELIVERY_ARRIVE:
                // 5~10取餐
                delayTime = (new Random().nextInt(6) + 5) * 60 * 1000;
                redisDelayQueue.push(DeliveryConstant.DelayTask.delivery_pickup_task, deliveryOrder.getId(), delayTime);
                logger.info("FollowDeliveryStatus设置取餐任务成功,id={},orderId={},delayTime={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), delayTime);
                break;
            case DeliveryConstant.KafkaEvent.DELIVERY_PICKUP:
                // 20~40送达
                delayTime = (new Random().nextInt(21) + 20) * 60 * 1000;
                redisDelayQueue.push(DeliveryConstant.DelayTask.delivery_complete_task, deliveryOrder.getId(), delayTime);
                logger.info("FollowDeliveryStatus设置送达任务成功,id={},orderId={},delayTime={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), delayTime);
                break;
            default:
                break;
        }
    }
}
