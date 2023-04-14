package com.cherish.consumer.delayqueue;

import com.cherish.component.consumer.IDelayQueueConsumer;
import com.cherish.constant.OrderConstant;
import com.cherish.dao.OrderDao;
import com.cherish.entity.order.Order;
import com.cherish.entity.rpc.Response;
import com.cherish.service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderConfirmExpiredConsumer implements IDelayQueueConsumer {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    private Logger logger = LoggerFactory.getLogger(OrderConfirmExpiredConsumer.class);

    @Override
    public String getTopic() {
        return OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_NAME;
    }

    @Override
    public void handle(List<Long> jobIds) {
        Map<Long, Order> orders = orderDao.getOrders(jobIds);
        for (Order order : orders.values()) {
            Response<Void> confirmExpiredResponse = orderService.merchantConfirmExpired(order);
            if (!confirmExpiredResponse.isSuccess())  logger.error("kafka消费者处理商家超时未接单失败,orderId:{},错误码:{}", order.getId(), confirmExpiredResponse.getCode());
            else logger.info("kafka消费组处理商家超时未接单成功,orderId:{}", order.getId());
        }
    }
}
