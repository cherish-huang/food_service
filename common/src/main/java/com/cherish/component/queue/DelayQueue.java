package com.cherish.component.queue;

import java.util.List;

public interface DelayQueue {
    boolean push(String topic, Object job, int delayTime);
    List<Long> pop(String topic, int count);

    boolean remove(String topic, Long jobId);
}
