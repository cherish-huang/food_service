package com.cherish.component.consumer;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Component
@ConditionalOnBean(ICanalConsumer.class)
public class CanalSubscriber {

    @Autowired
    private ICanalConsumer canalConsumer;

    @Value("${canal.hostname}")
    private String hostname;

    @Value("${canal.port}")
    private Integer port;

    @Value("${canal.table_reg}")
    private String tabReg = ".*\\..*";


//    @PostConstruct
    public void handler(){
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostname, port), "example", "", "");

        int batchSize = 1000;
        new Thread(()->{
            try {
                connector.connect();
                connector.subscribe(tabReg);
                connector.rollback();
                while (true) {
                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        try{
                            canalConsumer.handle(message.getEntries());
                        }catch (Exception exp){
                            exp.printStackTrace();
                            connector.rollback(batchId); // 处理失败, 回滚数据
                        }

                    }
                    connector.ack(batchId); // 提交确认
                }
            } finally {
                connector.disconnect();
            }
        }).start();
    }
}
