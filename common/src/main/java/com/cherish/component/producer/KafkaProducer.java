package com.cherish.component.producer;

import com.cherish.constant.CommonConstant;
import com.cherish.utils.JsonUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    public void send(String topic, String eventType, String key, Object data){
        String messageData = JsonUtils.toJson(data);
        ProducerRecord producerRecord = new ProducerRecord(topic, key, messageData);
        producerRecord.headers().add(CommonConstant.KafkaEvent.EVENT_TYPE_NAME, eventType.getBytes());
        try{
            kafkaTemplate.send(producerRecord);
        }catch (Exception e){
            logger.error("produce kafka error: {}", e.getMessage());
        }
//        logger.info("produce kafka message success,topic={},event_type={},data={}", topic, eventType, messageData);
    }
}
