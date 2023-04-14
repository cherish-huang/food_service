package com.cherish.service.order;

import com.cherish.entity.order.*;
import com.cherish.entity.rpc.Response;

import java.util.List;

public interface OrderService {

    Response<List<Order>> getOrders(List<Long> orderIds);

    Response<Long> submitOrder(SubmitOrderReq submitOrderReq);

    Response<Void> payOrder(PayOrderReq payOrderReq);

    Response<Void> approveOrder(Order order);

    Response<Void> merchantConfirmOrder(MerchantConfirmReq merchantConfirmReq);

    Response<Void> merchantConfirmExpired(Order order);

    Response<Void> confirmOrder(Order order);

    Response<Void> driverAccept(DriverAcceptReq driverAcceptReq);

    Response<Void> driverArrive(DriverArriveReq driverArriveReq);

    Response<Void> driverPickup(DriverPickupReq driverPickupReq);

    Response<Void> driverDeliver(DriverDeliverOrderReq driverDeliverOrderReq);

    Response<Void> cancelOrder(CancelOrderReq cancelOrderReq);

    Response<Void> completeOrder(Order order);

    Response<QueryOrderByStoreRes> queryOrderByStore(QueryOrderByStoreReq queryOrderByStoreReq);

}
