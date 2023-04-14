package com.cherish;

import com.cherish.component.producer.KafkaProducer;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
public class TestKafka {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testProducekafka(){
        kafkaProducer.send("order_event", OrderConstant.KafkaEvent.ORDER_CREATE, String.valueOf(4339693472165396482L), Order.builder().build());

    }
}
