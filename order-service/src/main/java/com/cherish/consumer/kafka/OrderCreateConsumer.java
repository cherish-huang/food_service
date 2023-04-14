package com.cherish.consumer.kafka;

import com.cherish.component.queue.impl.RedisDelayQueue;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.Order;
import com.cherish.utils.JsonUtils;
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
public class OrderCreateConsumer {
    @Autowired
    private RedisDelayQueue redisDelayQueue;

    private Set<String> ACCEPT_EVENT_TYPES= new HashSet<>(Arrays.asList(OrderConstant.KafkaEvent.ORDER_CREATE));

    @KafkaListener(topics = {CommonConstant.KafkaTopic.ORDER_EVENT}, groupId = "order_create")
    public void handleMessage(ConsumerRecord<String, String> record,
                              @Header(CommonConstant.KafkaEvent.EVENT_TYPE_NAME) String eventType){
        if(!ACCEPT_EVENT_TYPES.contains(eventType)) return;
        Order order = JsonUtils.toObject(record.value(), Order.class);
        //5~15中支付任务
        redisDelayQueue.push(OrderConstant.DelayTask.ORDER_PAY_NAME, order.getId(), (new Random().nextInt(11) + 5) * 60 * 1000);
    }
}
