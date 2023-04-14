package com.cherish.component.consumer;

import com.cherish.component.queue.impl.RedisDelayQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnBean(IDelayQueueConsumer.class)
@EnableScheduling
public class RedisDelayQueueSubscriber {

    @Autowired
    List<IDelayQueueConsumer> delayQueueConsumers;

    @Autowired
    private RedisDelayQueue redisDelayQueue;

    @Scheduled(cron = "*/1 * * * * ?")
    public void monitor(){
        for(IDelayQueueConsumer delayQueueConsumer: delayQueueConsumers){
            List<Long> jobIds = redisDelayQueue.pop(delayQueueConsumer.getTopic(), 100);
            if(jobIds.size() != 0){
                delayQueueConsumer.handle(jobIds);
            }
        }
    }
}
