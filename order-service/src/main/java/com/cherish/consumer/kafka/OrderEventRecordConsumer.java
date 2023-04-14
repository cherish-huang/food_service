package com.cherish.consumer.kafka;

import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.Order;
import com.cherish.utils.JsonUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

//@Component
public class OrderEventRecordConsumer {
    Logger logger = LoggerFactory.getLogger(OrderEventRecordConsumer.class);

//    @KafkaListener(topics = {CommonConstant.KafkaTopic.ORDER_EVENT}, groupId = "order_status_record")
//    public void handleMessage(ConsumerRecord<String, String> record,
//                              @Header(CommonConstant.KafkaEvent.EVENT_TYPE_NAME) String eventType){
//    }
}
