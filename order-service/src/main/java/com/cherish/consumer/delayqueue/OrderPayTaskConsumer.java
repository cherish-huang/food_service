package com.cherish.consumer.delayqueue;

import com.cherish.component.consumer.IDelayQueueConsumer;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.PayOrderReq;
import com.cherish.entity.rpc.Response;
import com.cherish.service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderPayTaskConsumer implements IDelayQueueConsumer {

    @Autowired
    private OrderService orderService;

    private Logger logger = LoggerFactory.getLogger(OrderPayTaskConsumer.class);
    @Override
    public String getTopic() {
        return OrderConstant.DelayTask.ORDER_PAY_NAME;
    }

    @Override
    public void handle(List<Long> jobIds) {
        for(long orderId: jobIds){
            Response<Void> payResponse = orderService.payOrder(PayOrderReq.builder().orderId(orderId).build());
            if(!payResponse.isSuccess()) logger.error("延时任务处理payOrder失败,orderId:{},错误码:{}", orderId, payResponse.getCode());
            else logger.info("延时任务处理payOrder成功,orderId:{}", orderId);
        }
    }
}
