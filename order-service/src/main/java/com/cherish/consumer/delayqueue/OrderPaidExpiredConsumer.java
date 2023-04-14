package com.cherish.consumer.delayqueue;

import com.cherish.component.consumer.IDelayQueueConsumer;
import com.cherish.constant.OrderConstant;
import com.cherish.dao.OrderDao;
import com.cherish.entity.order.CancelOrderReq;
import com.cherish.entity.rpc.Response;
import com.cherish.service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderPaidExpiredConsumer implements IDelayQueueConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;
    Logger logger = LoggerFactory.getLogger(OrderPaidExpiredConsumer.class);

    @Override
    public String getTopic() {
        return OrderConstant.DelayTask.ORDER_PAY_EXPIRED_NAME;
    }

    @Override
    public void handle(List<Long> jobIds) {
        for(long orderId: jobIds){
            CancelOrderReq cancelOrderReq = CancelOrderReq.builder()
                                                          .orderId(orderId)
                                                          .cancelSource(OrderConstant.CancelSource.SYSTEM)
                                                          .cancelReason(OrderConstant.CancelReason.PAID_TIMEOUT)
                                                          .build();
            Response<Void> cancelRes = orderService.cancelOrder(cancelOrderReq);
            if(!cancelRes.isSuccess()) logger.error("延时任务处理支付超时自动取消订单失败,orderId:{},错误码:{}", orderId, cancelRes.getCode());
            else logger.info("延时任务处理支付超时自动取消订单成功,orderId:{}", orderId);
        }
    }
}
