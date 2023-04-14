package com.cherish.service.delivery;

import com.cherish.entity.delivery.*;
import com.cherish.entity.rpc.Response;

import java.util.List;

public interface DeliveryService {
    Response<Long> createDelivery(CreateDeliveryReq createDeliveryReq);

    Response<Void> acceptDelivery(AcceptDeliveryReq acceptDeliveryReq);

    Response<Void> arriveDelivery(ArriveDeliveryReq arriveDeliveryReq);

    Response<Void> pickupDelivery(PickupDeliveryReq pickupDeliveryReq);

    Response<Void> completeDelivery(CompleteDeliveryReq completeDeliveryReq);

    Response<Void> cancelDelivery(CancelDeliveryReq cancelDeliveryReq);

    Response<List<DeliveryOrder>> queryDeliveryOrders(QueryDeliveryOrderReq queryDeliveryOrderReq);
//    Response<List<DeliveryOrder>> queryDeliveryOrders(QueryDeliveryOrderByIdReq queryDeliveryOrderByOrderIdReq);

    Response<List<DeliveryOrder>> queryDeliveryOrders(QueryDeliveryOrderByOrderIdReq queryDeliveryOrderByOrderIdReq);
}
