package com.cherish.component.queue.impl;

import com.cherish.component.queue.DelayQueue;
import com.cherish.component.redis.RedisManager;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnClass(RedisManager.class)
@ConditionalOnBean(RedisManager.class)
public class RedisDelayQueue implements DelayQueue {

    @Autowired
    private RedisManager redisManager;

    @Override
    public boolean push(String topic, Object job, int delayTime) {
        if(Strings.isEmpty(topic)) return false;
        long score = System.currentTimeMillis() + delayTime;
        return redisManager.zadd(topic, String.valueOf(job), score);
    }

    @Override
    public List<Long> pop(String topic, int count) {
        Set<String> members = redisManager.zrange(topic, 0, System.currentTimeMillis(), count);
        return members.stream().filter(member -> redisManager.zremove(topic, member) == 1)
                               .map(str->Long.parseLong(str)).collect(Collectors.toList());
    }

    @Override
    public boolean remove(String topic, Long jobId) {
        return redisManager.zremove(topic, String.valueOf(jobId)) != 0;
    }
}
