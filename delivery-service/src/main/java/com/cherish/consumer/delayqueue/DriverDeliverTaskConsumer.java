package com.cherish.consumer.delayqueue;

import com.cherish.component.consumer.IDelayQueueConsumer;
import com.cherish.constant.DeliveryConstant;
import com.cherish.dao.DeliveryDao;
import com.cherish.entity.delivery.CompleteDeliveryReq;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.rpc.Response;
import com.cherish.service.delivery.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DriverDeliverTaskConsumer implements IDelayQueueConsumer {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryDao deliveryDao;

    Logger logger = LoggerFactory.getLogger(DriverDeliverTaskConsumer.class);

    @Override
    public String getTopic() {
        return DeliveryConstant.DelayTask.delivery_complete_task;
    }

    @Override
    public void handle(List<Long> jobIds) {
        List<DeliveryOrder> deliveryOrders = deliveryDao.getDeliveryOrders(jobIds);
        for(DeliveryOrder deliveryOrder: deliveryOrders){
            Response<Void> response = deliveryService.completeDelivery(CompleteDeliveryReq.builder().deliveryOrderId(deliveryOrder.getId()).build());
            if(response.isSuccess()) logger.info("DriverDeliverTaskConsumer处理completeDelivery成功,id={},orderId={}", deliveryOrder.getId(), deliveryOrder.getOrderId());
            else logger.error("DriverDeliverTaskConsumer处理completeDelivery失败,id={},orderId={},code={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), response.getCode());
        }
    }
}
