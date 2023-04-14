package com.cherish.component.consumer;

import java.util.List;

public interface IDelayQueueConsumer {
    String getTopic();
    void handle(List<Long> jobIds);

}
