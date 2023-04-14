package com.cherish.link;

import com.cherish.TestApp;
import com.cherish.entity.delivery.*;
import com.cherish.entity.rpc.Response;
import com.cherish.service.delivery.DeliveryService;
import com.cherish.utils.JsonUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.midi.Track;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
public class DriverOpOrder {
    @DubboReference(check = false)
    private DeliveryService deliveryService;

    Logger logger = LoggerFactory.getLogger(DriverOpOrder.class);

    long driverId = 212698752L;

    long deliveryOrderId = 4332459714098053121L;

    @Test
    public void driverOpOrder() throws InterruptedException {
        while (true){
            Response<List<DeliveryOrder>> deliveryResponse = deliveryService.queryDeliveryOrders(QueryDeliveryOrderReq.builder().size(1).build());
            if(!deliveryResponse.isSuccess()){
                logger.error("queryDeliveryOrders失败,code={}", deliveryResponse.getCode());
                break;
            }
            if(deliveryResponse.getData().isEmpty()){
                Thread.sleep(1000);
            }

            for(DeliveryOrder deliveryOrder: deliveryResponse.getData()){
                deliveryOrderId = deliveryOrder.getId();
                logger.info("queryDeliveryOrders成功,deliveryOrder={}", JsonUtils.toJson(deliveryOrder));
                return;
            }
            Thread.sleep(1000);
        }
    }
    @Test
    public void acceptDelivery(){
        Response<Void> acceptDeliveryResponse = deliveryService.acceptDelivery(AcceptDeliveryReq.builder().deliveryOrderId(deliveryOrderId).driverId(driverId).build());
        if(acceptDeliveryResponse.isSuccess()) logger.info("acceptDelivery成功");
        else logger.error("acceptDelivery失败,code={}", acceptDeliveryResponse.getCode());
    }

    @Test
    public void arriveDelivery(){
        Response<Void> arriveDeliveryResponse = deliveryService.arriveDelivery(ArriveDeliveryReq.builder().deliveryOrderId(deliveryOrderId).build());
        if(arriveDeliveryResponse.isSuccess()) logger.info("arriveDelivery成功");
        else logger.error("arriveDelivery失败,code={}", arriveDeliveryResponse.getCode());
    }

    @Test
    public void pickupDelivery(){
        Response<Void> pickupDeliveryResponse = deliveryService.pickupDelivery(PickupDeliveryReq.builder().deliveryOrderId(deliveryOrderId).build());
        if(pickupDeliveryResponse.isSuccess()) logger.info("pickupDelivery成功");
        else logger.error("pickupDelivery失败,code={}", pickupDeliveryResponse.getCode());
    }

    @Test
    public void completeDelivery(){
        Response<Void> completeDeliveryResponse = deliveryService.completeDelivery(CompleteDeliveryReq.builder().deliveryOrderId(deliveryOrderId).build());
        if(completeDeliveryResponse.isSuccess()) logger.info("completeDelivery成功");
        else logger.error("completeDelivery失败,code={}", completeDeliveryResponse.getCode());
    }
}
