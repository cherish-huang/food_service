package com.cherish.component.consumer;

import java.util.List;

import com.alibaba.otter.canal.protocol.CanalEntry;

public interface ICanalConsumer {

    void handle(List<CanalEntry.Entry> entrys) throws Exception;

}
