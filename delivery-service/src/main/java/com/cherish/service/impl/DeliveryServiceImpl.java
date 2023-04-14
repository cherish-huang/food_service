package com.cherish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.component.producer.KafkaProducer;
import com.cherish.component.rpc.RpcHandler;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.DeliveryConstant;
import com.cherish.dao.DeliveryDao;
import com.cherish.entity.delivery.*;
import com.cherish.entity.order.Order;
import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.Store;
import com.cherish.error.BaseError;
import com.cherish.error.DeliveryErrCode;
import com.cherish.service.delivery.DeliveryService;
import com.cherish.service.idgen.IDGenService;
import com.cherish.service.order.OrderService;
import com.cherish.service.store.StoreService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DubboService
public class DeliveryServiceImpl implements DeliveryService {

    @DubboReference(check = false)
    private StoreService storeService;

    @DubboReference(check = false)
    private IDGenService idGenService;

    @DubboReference(check = false)
    private OrderService orderService;

    @Autowired
    private DeliveryDao deliveryDao;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Override
    @RpcHandler
    public Response<Long> createDelivery(CreateDeliveryReq createDeliveryReq) {
        //1.查询store
        Response<Store> storeResponse = storeService.queryStore(createDeliveryReq.getStoreId());
        if(!storeResponse.isSuccess()) return Response.error(DeliveryErrCode.ERROR_QUERY_STORE);
        //2.构造新的delivery单
        DeliveryOrder newDeliveryOrder = buildDeliveryOrder(createDeliveryReq, storeResponse.getData());
        //3.插入delivery单
        if(deliveryDao.insertDeliveryOrder(newDeliveryOrder) == 0)
            return Response.error(DeliveryErrCode.ERROR_INSERT_DELIVERY);
        //4.发送kafka事件
        sendOrderEvent(DeliveryConstant.KafkaEvent.DELIVERY_CREATE, newDeliveryOrder.getId());
        return Response.success(newDeliveryOrder.getId());
    }

    @Override
    @RpcHandler
    public Response<Void> acceptDelivery(AcceptDeliveryReq acceptDeliveryReq) {
        //1.获取deliveryOrder信息
        DeliveryOrder deliveryOrder = deliveryDao.getDeliveryOrder(acceptDeliveryReq.getDeliveryOrderId());
        if(deliveryOrder == null) return Response.error(DeliveryErrCode.ERROR_GET_DELIVERY);
        //2.判断运单deliveryOrder状态是否是Created才能进行接单
        if(deliveryOrder.getStatus() != DeliveryConstant.DeliveryOrderStatus.CREATED){
            return Response.error(DeliveryErrCode.ERROR_DELIVERY_STATUS);
        }
        //3.构建driverOrder信息
        DriverOrder driverOrder = buildDriverOrder(acceptDeliveryReq, deliveryOrder);
        //4.生成deliveryOrder变更条件和变更字段
        DeliveryOrder srcDeliveryOrder = DeliveryOrder.builder()
                                                      .id(acceptDeliveryReq.getDeliveryOrderId())
                                                      .status(DeliveryConstant.DeliveryOrderStatus.CREATED)
                                                      .build();
        DeliveryOrder dstDeliveryOrder = DeliveryOrder.builder()
                                                      .driverId(acceptDeliveryReq.getDriverId())
                                                      .driverOrderId(driverOrder.getId())
                                                      .status(DeliveryConstant.DeliveryOrderStatus.CONFIRMED)
                                                      .assignTime(System.currentTimeMillis())
                                                      .build();
        //5.执行插入driverOrder并且更新deliveryOrder
        if (deliveryDao.insertDriverAndUpdateDeliveryOrder(driverOrder, srcDeliveryOrder, dstDeliveryOrder) == 0)
            return Response.error(DeliveryErrCode.ERROR_ACCEPT_DELIVERY_ORDER);

        //6.发送kafka事件
        sendOrderEvent(DeliveryConstant.KafkaEvent.DELIVERY_ACCEPT, acceptDeliveryReq.getDeliveryOrderId());

        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> arriveDelivery(ArriveDeliveryReq arriveDeliveryReq) {
        //1.获取deliveryOrder信息
        DeliveryOrder deliveryOrder = deliveryDao.getDeliveryOrder(arriveDeliveryReq.getDeliveryOrderId());
        if(deliveryOrder == null) return Response.error(DeliveryErrCode.ERROR_GET_DELIVERY);
        //2.判断运单deliveryOrder状态是否是已经Accepted才能进行接单
        if(deliveryOrder.getStatus() != DeliveryConstant.DeliveryOrderStatus.CONFIRMED){
            return Response.error(DeliveryErrCode.ERROR_DELIVERY_STATUS);
        }
        //3.生成deliveryOrder变更条件和变更字段
        DeliveryOrder srcDeliveryOrder = DeliveryOrder.builder()
                                                      .id(arriveDeliveryReq.getDeliveryOrderId())
                                                      .status(DeliveryConstant.DeliveryOrderStatus.CONFIRMED)
                                                      .build();
        DeliveryOrder dstDeliveryOrder = DeliveryOrder.builder()
                                                      .status(DeliveryConstant.DeliveryOrderStatus.ARRIVED)
                                                      .arriveTime(System.currentTimeMillis())
                                                      .build();
        //4.更新deliveryOrder
        if(deliveryDao.updateDeliveryOrder(dstDeliveryOrder, srcDeliveryOrder) == 0)
            return Response.error(DeliveryErrCode.ERROR_ACCEPT_DELIVERY_ORDER);

        //5.发送kafka事件
        sendOrderEvent(DeliveryConstant.KafkaEvent.DELIVERY_ARRIVE, arriveDeliveryReq.getDeliveryOrderId());

        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> pickupDelivery(PickupDeliveryReq pickupDeliveryReq) {
        //1.获取deliveryOrder信息
        DeliveryOrder deliveryOrder = deliveryDao.getDeliveryOrder(pickupDeliveryReq.getDeliveryOrderId());
        if(deliveryOrder == null) return Response.error(DeliveryErrCode.ERROR_GET_DELIVERY);
        //2.判断运单deliveryOrder状态是否是已经Arrived才能进行接单
        if(deliveryOrder.getStatus() != DeliveryConstant.DeliveryOrderStatus.ARRIVED){
            return Response.error(DeliveryErrCode.ERROR_DELIVERY_STATUS);
        }
        //3.判断在DFF模式下，商家必须先接单才能进行pickup
        if(deliveryOrder.isDff()){
            //4.1请求order服务查询订单详情
            Response<List<Order>> ordersResponse = orderService.getOrders(Arrays.asList(deliveryOrder.getOrderId()));
            if(!ordersResponse.isSuccess() || ordersResponse.getData().isEmpty()) return Response.error(DeliveryErrCode.ERROR_GET_ORDER);
            //商家还没有接单
            if(ordersResponse.getData().get(0).getMerchantConfirmTime() == 0) return Response.error(DeliveryErrCode.ERROR_DELIVERY_STATUS);
        }
        //4.生成deliveryOrder变更条件和变更字段
        DeliveryOrder srcDeliveryOrder = DeliveryOrder.builder()
                                                      .id(pickupDeliveryReq.getDeliveryOrderId())
                                                      .status(DeliveryConstant.DeliveryOrderStatus.ARRIVED)
                                                      .build();
        DeliveryOrder dstDeliveryOrder = DeliveryOrder.builder()
                                                      .status(DeliveryConstant.DeliveryOrderStatus.PICKED)
                                                      .pickupTime(System.currentTimeMillis())
                                                      .build();
        //5.更新deliveryOrder
        if(deliveryDao.updateDeliveryOrder(dstDeliveryOrder, srcDeliveryOrder) == 0)
            return Response.error(DeliveryErrCode.ERROR_ACCEPT_DELIVERY_ORDER);

        //6.发送kafka事件
        sendOrderEvent(DeliveryConstant.KafkaEvent.DELIVERY_PICKUP, pickupDeliveryReq.getDeliveryOrderId());

        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> completeDelivery(CompleteDeliveryReq completeDeliveryReq) {
        //1.获取deliveryOrder信息
        DeliveryOrder deliveryOrder = deliveryDao.getDeliveryOrder(completeDeliveryReq.getDeliveryOrderId());
        if(deliveryOrder == null) return Response.error(DeliveryErrCode.ERROR_GET_DELIVERY);
        //2.判断运单deliveryOrder状态是否是已经Arrived才能进行接单
        if(deliveryOrder.getStatus() != DeliveryConstant.DeliveryOrderStatus.PICKED){
            return Response.error(DeliveryErrCode.ERROR_DELIVERY_STATUS);
        }
        //3.生成deliveryOrder变更条件和变更字段
        DeliveryOrder dstDeliveryOrder = DeliveryOrder.builder()
                                                      .status(DeliveryConstant.DeliveryOrderStatus.COMPLETED)
                                                      .completeTime(System.currentTimeMillis())
                                                      .build();
        DeliveryOrder srcDeliveryOrder = DeliveryOrder.builder()
                                                      .id(completeDeliveryReq.getDeliveryOrderId())
                                                      .status(DeliveryConstant.DeliveryOrderStatus.PICKED)
                                                      .build();
        //4.生成driverOrder变更条件和变更字段
        DriverOrder dstDriverOrder = DriverOrder.builder()
                                                .status(DeliveryConstant.DriverOrderStatus.COMPLETED)
                                                .build();
        DriverOrder srcDriverOrder = DriverOrder.builder()
                                                .id(deliveryOrder.getDriverOrderId())
                                                .status(DeliveryConstant.DriverOrderStatus.CONFIRMED)
                                                .build();

        //5.更新deliveryOrder和driverOrder
        if(deliveryDao.updateDeliveryDriverOrder(dstDeliveryOrder, srcDeliveryOrder, dstDriverOrder, srcDriverOrder) == 0)
            return Response.error(DeliveryErrCode.ERROR_ACCEPT_DELIVERY_ORDER);

        //6.发送kafka事件
        sendOrderEvent(DeliveryConstant.KafkaEvent.DELIVERY_COMPLETE, completeDeliveryReq.getDeliveryOrderId());

        return Response.success(null);
    }

    @Override
    public Response<Void> cancelDelivery(CancelDeliveryReq cancelDeliveryReq) {
        //1.获取deliveryOrder信息
        DeliveryOrder deliveryOrder = deliveryDao.getDeliveryOrder(cancelDeliveryReq.getDeliveryOrderId());
        if(deliveryOrder == null) return Response.error(DeliveryErrCode.ERROR_GET_DELIVERY);
        //2.获取运单已经取消或者已经完成，则不能再取消运单
        if(deliveryOrder.getStatus() == DeliveryConstant.DeliveryOrderStatus.CANCELED || deliveryOrder.getStatus() == DeliveryConstant.DeliveryOrderStatus.COMPLETED)
                return Response.error(DeliveryErrCode.ERROR_DELIVERY_STATUS);
        //3.生成deliveryOrder变更条件和变更字段
        DeliveryOrder dstDeliveryOrder = DeliveryOrder.builder()
                                                    .status(DeliveryConstant.DeliveryOrderStatus.CANCELED)
                                                    .cancelSource(cancelDeliveryReq.getCancelSource())
                                                    .cancelReason(cancelDeliveryReq.getCancelReason())
                                                    .cancelTime(System.currentTimeMillis())
                                                    .build();
        DeliveryOrder srcDeliveryOrder = DeliveryOrder.builder()
                                                      .id(cancelDeliveryReq.getDeliveryOrderId())
                                                      .status(deliveryOrder.getStatus())
                                                      .build();

        //4.如果运单处于create状态, 则直接更新deliverOrder即可
        if(deliveryOrder.getStatus() == DeliveryConstant.DeliveryOrderStatus.CREATED){
            if(deliveryDao.updateDeliveryOrder(dstDeliveryOrder, srcDeliveryOrder) == 0) return Response.error(DeliveryErrCode.ERROR_ACCEPT_DELIVERY_ORDER);
        }
        //5.运单已经指派了，则需要更新deliveryOrder和driverOrder
        else{
            DriverOrder dstDriverOrder = DriverOrder.builder()
                                                    .status(DeliveryConstant.DriverOrderStatus.CANCELED)
                                                    .build();
            DriverOrder srcDriverOrder = DriverOrder.builder()
                                                    .id(deliveryOrder.getDriverOrderId())
                                                    .build();
            if(deliveryDao.updateDeliveryDriverOrder(dstDeliveryOrder, srcDeliveryOrder, dstDriverOrder, srcDriverOrder) == 0) return Response.error(DeliveryErrCode.ERROR_ACCEPT_DELIVERY_ORDER);
        }

        //6.发送kafka事件
        sendOrderEvent(DeliveryConstant.KafkaEvent.DELIVERY_CANCEL, cancelDeliveryReq.getDeliveryOrderId());

        return Response.success(null);
    }

    @Override
    @RpcHandler(isRecordLog = false)
    public Response<List<DeliveryOrder>> queryDeliveryOrders(QueryDeliveryOrderReq queryDeliveryOrderReq){
        List<DeliveryOrder> deliveryOrders = new ArrayList<>();
        int leave = queryDeliveryOrderReq.getSize();
        LambdaQueryWrapper<DeliveryOrder> queryWrapper = Wrappers.<DeliveryOrder>lambdaQuery().eq(DeliveryOrder::getStatus, DeliveryConstant.DeliveryOrderStatus.CREATED);
        for(int shardId = 0; shardId < CommonConstant.DbShardNum.DELIVERY_ORDER; shardId++){
            List<DeliveryOrder> temDeliveryOrders = deliveryDao.queryDeliveries(queryWrapper, shardId, leave);
            deliveryOrders.addAll(temDeliveryOrders);
            leave -= temDeliveryOrders.size();
            if(leave <= 0) break;
        }
        return Response.success(deliveryOrders);
    }

    @Override
    public Response<List<DeliveryOrder>> queryDeliveryOrders(QueryDeliveryOrderByOrderIdReq queryDeliveryOrderByOrderIdReq) {
        return Response.success(deliveryDao.getDeliveryOrdersByOrderIds(queryDeliveryOrderByOrderIdReq.getIds()));
    }

    private void sendOrderEvent(String eventType, long deliveryOrderId){
        DeliveryOrder deliveryOrder = deliveryDao.getDeliveryOrder(deliveryOrderId);
        kafkaProducer.send(CommonConstant.KafkaTopic.DELIVERY_EVENT, eventType, String.valueOf(deliveryOrderId), deliveryOrder);
    }


    private DeliveryOrder buildDeliveryOrder(CreateDeliveryReq createDeliveryReq, Store store){
        Response<Long> idResponse = idGenService.genID(CommonConstant.IdServiceType.ID_DELIVERY_SERVICE_TYPE);
        if(!idResponse.isSuccess()) throw BaseError.newBaseError(DeliveryErrCode.ERROR_GEN_DELIVERY_ID);
        DeliveryOrder deliveryOrder =  DeliveryOrder.builder()
                                                    .id(idResponse.getData() * CommonConstant.DbShardNum.DELIVERY_ORDER + createDeliveryReq.getOrderId() % CommonConstant.DbShardNum.DELIVERY_ORDER)
                                                    .orderId(createDeliveryReq.getOrderId())
                                                    .status(DeliveryConstant.DeliveryOrderStatus.CREATED)
                                                    .deliveryName(createDeliveryReq.getDeliveryName())
                                                    .deliveryPhone(createDeliveryReq.getDeliveryPhone())
                                                    .deliveryAddress(createDeliveryReq.getDeliveryAddress())
                                                    .deliveryLatitude(createDeliveryReq.getDeliveryLatitude())
                                                    .deliveryLongitude(createDeliveryReq.getDeliveryLongitude())
                                                    .storeAddress(store.getAddress())
                                                    .storeLatitude(store.getLatitude())
                                                    .storeLongitude(store.getLongitude())
                                                    .createTime(System.currentTimeMillis())
                                                    .build();
        if(createDeliveryReq.isDff()) deliveryOrder.setDff();
        return deliveryOrder;
    }

    private DriverOrder buildDriverOrder(AcceptDeliveryReq acceptDeliveryReq, DeliveryOrder deliveryOrder){
        Response<Long> idResponse = idGenService.genID(CommonConstant.IdServiceType.ID_DELIVERY_SERVICE_TYPE);
        if(!idResponse.isSuccess()) throw BaseError.newBaseError(DeliveryErrCode.ERROR_GEN_DRIVER_ORDER_ID);
        return DriverOrder.builder()
                .id(idResponse.getData() * CommonConstant.DbShardNum.DRIVER_ORDER + acceptDeliveryReq.getDriverId() % CommonConstant.DbShardNum.DRIVER_ORDER)
                .status(DeliveryConstant.DriverOrderStatus.CONFIRMED)
                .driverId(acceptDeliveryReq.getDriverId())
                .deliveryOrderId(acceptDeliveryReq.getDeliveryOrderId())
                .deliveryFee(3.0)
                .createTime(System.currentTimeMillis())
                .build();
    }

}
