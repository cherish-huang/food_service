package com.cherish.consumer.delayqueue;

import com.cherish.component.consumer.IDelayQueueConsumer;
import com.cherish.constant.DeliveryConstant;
import com.cherish.dao.DeliveryDao;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.delivery.PickupDeliveryReq;
import com.cherish.entity.rpc.Response;
import com.cherish.service.delivery.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DriverPickupTaskConsumer implements IDelayQueueConsumer {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryDao deliveryDao;

    Logger logger = LoggerFactory.getLogger(DriverPickupTaskConsumer.class);
    @Override
    public String getTopic() {
        return DeliveryConstant.DelayTask.delivery_pickup_task;
    }

    @Override
    public void handle(List<Long> jobIds) {
        List<DeliveryOrder> deliveryOrders = deliveryDao.getDeliveryOrders(jobIds);
        for(DeliveryOrder deliveryOrder: deliveryOrders){
            Response<Void> response = deliveryService.pickupDelivery(PickupDeliveryReq.builder().deliveryOrderId(deliveryOrder.getId()).build());
            if(response.isSuccess()) logger.info("DriverArriveTaskConsumer处理pickupDelivery成功,id={},orderId={}", deliveryOrder.getId(), deliveryOrder.getOrderId());
            else logger.error("DriverArriveTaskConsumer处理pickupDelivery失败,id={},orderId={},code={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), response.getCode());
        }
    }
}
