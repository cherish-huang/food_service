package com.cherish.service.impl;

import com.cherish.constant.CommonConstant;
import com.cherish.component.rpc.RpcHandler;
import com.cherish.component.producer.KafkaProducer;
import com.cherish.component.queue.impl.RedisDelayQueue;
import com.cherish.constant.OrderConstant;
import com.cherish.dao.OrderDao;
import com.cherish.entity.cart.CartItem;
import com.cherish.entity.delivery.CreateDeliveryReq;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.delivery.QueryDeliveryOrderByOrderIdReq;
import com.cherish.entity.order.*;
import com.cherish.entity.rpc.Response;
import com.cherish.entity.store.StoreItem;
import com.cherish.entity.store.Store;
import com.cherish.error.BaseError;
import com.cherish.error.OrderErrCode;
import com.cherish.service.cart.CartService;
import com.cherish.service.delivery.DeliveryService;
import com.cherish.service.idgen.IDGenService;
import com.cherish.service.order.OrderService;
import com.cherish.service.store.StoreItemService;
import com.cherish.service.store.StoreService;
import com.cherish.utils.JsonUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@DubboService
public class OrderServiceImpl implements OrderService {

    @DubboReference(check = false)
    private StoreService storeService;

    @DubboReference(check = false)
    private StoreItemService itemService;

    @DubboReference(check = false)
    private CartService cartService;

    @DubboReference(check = false)
    private IDGenService idGenService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private RedisDelayQueue redisDelayQueue;

    @Autowired
    private OrderDao orderDao;

    @DubboReference(check = false)
    private DeliveryService deliveryService;

    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public Response<List<Order>> getOrders(List<Long> orderIds) {
        return Response.success(new ArrayList<>(orderDao.getOrders(orderIds).values()));
    }

    @Override
    @RpcHandler
//    @SentinelResource(value = "order.submit")
    public Response<Long> submitOrder(SubmitOrderReq submitOrderReq) {
        //1. 查询商铺
        Response<Store> storeRes = storeService.queryStore(submitOrderReq.getStoreId());
        if(!storeRes.isSuccess() || storeRes.getData() == null) return Response.error(OrderErrCode.ERROR_QUERY_STORE);
        //2.将购物车所有的菜品组装起来批量查询菜品
        List<Long> item_ids = submitOrderReq.getCartItems().stream().map(CartItem::getItemId).collect(Collectors.toList());
        Response<List<StoreItem>> itemRes = itemService.queryItems(item_ids);
        if(!itemRes.isSuccess()) return Response.error(OrderErrCode.ERROR_QUERY_ITEM);
        //3.将item映射成 id -> item并且检验商品是否都存在
        Map<Long, StoreItem> itemMap = itemRes.getData().stream().collect(Collectors.toMap(StoreItem::getId, Function.identity()));
        if (submitOrderReq.getCartItems().stream().anyMatch(s -> !itemMap.containsKey(s.getItemId()))) return Response.error(OrderErrCode.ERROR_QUERY_ITEM);
        //4.生成订单信息
        Order newOrder = buildOrder(submitOrderReq, submitOrderReq.getCartItems(), itemMap);
        //5.生成订单商品信息
        List<OrderItem> orderItems = buildOrderItems(submitOrderReq.getCartItems(), newOrder, itemMap);
        //6.数据库事务将订单表，订单商户关联表和订单item表插入
        if(orderDao.insertOrder(newOrder, orderItems) == 0) return Response.error(OrderErrCode.ERROR_INSERT_ORDER);
        //7.向延时队列推送10分钟未支付订单自动取消任务
        redisDelayQueue.push(OrderConstant.DelayTask.ORDER_PAY_EXPIRED_NAME, newOrder.getId(),
                             OrderConstant.DelayTask.ORDER_PAY_EXPIRED_VALUE);
        //8.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_CREATE, newOrder.getId());
        //9. 清空购物车
        cartService.emptyCartItem(submitOrderReq.getBuyerId(), submitOrderReq.getStoreId());
        return Response.success(newOrder.getId());
    }

    @Override
    @RpcHandler
    public Response<Void> payOrder(PayOrderReq payOrderReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(payOrderReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2. 判断订单是否满足取消条件
        if(!canPay(order)) return Response.error(OrderErrCode.ERROR_PAY_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order dstOrder = Order.builder().status(OrderConstant.OrderStatus.PAID)
                                        .payTime(currentTime)
                                        .statusUpdateTime(currentTime)
                                        .build();
        Order srcOrder = Order.builder().id(order.getId()).status(OrderConstant.OrderStatus.CREATED).storeId(order.getStoreId()).build();
        //4.判断数据库是否更新成功
        if(orderDao.updateOrder(dstOrder, srcOrder) == 0) return Response.error(OrderErrCode.ERROR_PAY_STATUS);
        //5.移除延时队列的超时支付取消订单任务
        redisDelayQueue.remove(OrderConstant.DelayTask.ORDER_PAY_EXPIRED_NAME, order.getId());
        //6.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_PAY, payOrderReq.getOrderId());
        return Response.success(null);
    }

    @Override
    public Response<Void> approveOrder(Order order) {
        //1.判断订单是否存在
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2. 判断订单是否满足approve条件
        if(!canApprove(order))  return Response.error(OrderErrCode.ERROR_APPROVE_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order.OrderBuilder dstOrderBuilder = Order.builder().status(OrderConstant.OrderStatus.APPROVED)
                                                            .approveTime(currentTime)
                                                            .statusUpdateTime(currentTime);
        Order srcOrder = Order.builder().id(order.getId()).status(OrderConstant.OrderStatus.PAID).storeId(order.getStoreId()).build();
        //4.MFF模式下approve状态之后订单对商家就可见了
        if(!order.isDff()){
            dstOrderBuilder.isMerchantVisible(true)
                             //对于自动确认的订单merchantDeadline应该是当前时间，便于补偿定时器可以扫到
                            .merchantDeadline(currentTime + (order.isAutoConfirm()? 10 * 1000: OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_VALUE));
        }
        //5.判断数据库更新是否成功
        if(orderDao.updateOrder(dstOrderBuilder.build(), srcOrder) == 0)  return Response.error(OrderErrCode.ERROR_APPROVE_STATUS);
        //6.发送kafka approve事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_APPROVE, order.getId());
        //7.订单流DFF模式下，订单approve需要先通知运单创建运单，开始进行指派流程
        if(order.isDff()){
            Response<Long> deliveryResponse = deliveryService.createDelivery(buildDeliveryReq(order));
            if(!deliveryResponse.isSuccess())
                logger.warn("approveOrder在DFF模式下创建运单失败,id={},code={}", order.getId(), deliveryResponse.getCode());
        }
        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> merchantConfirmOrder(MerchantConfirmReq merchantConfirmReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(merchantConfirmReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2.confirm订单
        Response<Void> confirmResponse = confirmOrder(order);
        //3.1 confirm订单成功则移除商家接单超时任务
        if(confirmResponse.isSuccess()) redisDelayQueue.remove(OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_NAME, order.getId());

        return confirmResponse;
    }

    @Override
    public Response<Void> merchantConfirmExpired(Order order){
        //补偿自动接单，直接confirm订单
        if(order.isAutoConfirm()){
            Response<Void> confirmResponse = confirmOrder(order);
            if(confirmResponse.isSuccess()) logger.info("merchantConfirmExpired autoConfirm订单成功,id={}", order.getId());
            else logger.error("merchantConfirmExpired autoConfirm订单失败,id={},code={}", order.getId(), confirmResponse.getCode());
            return confirmResponse;
        }
        //补偿手动接单超时取消
        if(order.isOvertimeCancel()){
            long currentTime = System.currentTimeMillis();
            Order dstOrder = Order.builder().status(OrderConstant.OrderStatus.CANCELED)
                    .cancelTime(currentTime)
                    .cancelSource(OrderConstant.CancelSource.SYSTEM)
                    .cancelReason(OrderConstant.CancelReason.MERCHANT_CONFIRM_EXPIRED)
                    .statusUpdateTime(currentTime)
                    .build();
            Order srOrder = Order.builder().id(order.getId()).storeId(order.getStoreId()).status(order.getStatus()).build();
            if(orderDao.updateOrder(dstOrder, srOrder) != 0){
                sendOrderEvent(OrderConstant.KafkaEvent.ORDER_CANCEL, order.getId());
                logger.info("merchantConfirmExpired cancel订单处理成功,id={}", order.getId());
                return Response.success(null);
            }else{
                logger.error("merchantConfirmExpired cancel订单处理失败,id={}", order.getId());
                return Response.error(OrderErrCode.ERROR_DEAL_MANUAL_CANCEL_ORDER);
            }
        }
        //补偿手动接单超时confirm
        Response<Void> confirmResponse = confirmOrder(order);
        if(confirmResponse.isSuccess()) logger.info("merchantConfirmExpired confirm订单成功,id={}", order.getId());
        else logger.error("merchantConfirmExpired confirm订单失败,id={},code={}", order.getId(), confirmResponse.getCode());
        return confirmResponse;
    }

    @Override
    @RpcHandler
    public Response<Void> driverAccept(DriverAcceptReq driverAcceptReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(driverAcceptReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2. 判断订单是否满足accept条件
        if(!canDriverAccept(order)) return Response.error(OrderErrCode.ERROR_DRIVER_ACCEPT_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order.OrderBuilder dstOrderBuilder = Order.builder().status(OrderConstant.OrderStatus.DRIVER_ACCEPTED)
                .driverAcceptTime(driverAcceptReq.getDriverAcceptTime()==null? currentTime: driverAcceptReq.getDriverAcceptTime())
                .statusUpdateTime(currentTime);
        Order.OrderBuilder srcOrderBuilder = Order.builder().id(order.getId()).storeId(order.getStoreId());
        //3.1在dff模式下骑手接单成功后商家可以看见订单
        if(order.isDff()){
            dstOrderBuilder.isMerchantVisible(true)
                          //对于自动确认的订单merchantDeadline应该是当前时间，便于补偿定时器可以扫到
                          .merchantDeadline(currentTime + (order.isAutoConfirm()? 10 * 1000: OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_VALUE));
            //dff模式下骑手接单的上一个状态是approve
            srcOrderBuilder.status(OrderConstant.OrderStatus.APPROVED);
        }
        //3.2mff模式下骑手接单的上一个状态是商家confirm
        else srcOrderBuilder.status(OrderConstant.OrderStatus.MERCHANT_CONFIRMED);

        //4.判断数据库更新数量是否符合预期
        if(orderDao.updateOrder(dstOrderBuilder.build(), srcOrderBuilder.build()) == 0) return Response.error(OrderErrCode.ERROR_ORDER_UPDATE_NUM);
        //5.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_DRIVER_ACCEPT, order.getId());

        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> driverArrive(DriverArriveReq driverArriveReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(driverArriveReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2. 判断订单是否满足driverArrive条件
        if(!canDriverArrive(order)) return Response.error(OrderErrCode.ERROR_DRIVER_ARRIVE_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order.OrderBuilder dstOrderBuilder = Order.builder().status(OrderConstant.OrderStatus.DRIVER_ARRIVED)
                                                            .driverArriveTime(currentTime)
                                                            .statusUpdateTime(currentTime);
        Order srcOrder = Order.builder().id(order.getId()).status(order.getStatus()).storeId(order.getStoreId()).build();
        //4.订单表中骑手还没有接单，则需要补偿骑手接单状态
        if(order.getDriverAcceptTime() == 0){
            //4.1同步骑手接单的数据
            dstOrderBuilder.driverAcceptTime(driverArriveReq.getDriverAcceptTime());
            //4.2如果是DFF这时候还需要让merchant可以看见订单,并且设置接单超时
            if(order.isDff()){
                dstOrderBuilder.isMerchantVisible(true)
                        //对于自动确认的订单merchantDeadline应该是当前时间，便于补偿定时器可以扫到
                        .merchantDeadline(currentTime + (order.isAutoConfirm()? 10 * 1000: OrderConstant.DelayTask.ORDER_MERCHANT_CONFIRM_EXPIRED_VALUE));
            }
            logger.info("driverArrive补偿骑手状态成功，id={}, dstOrder={}", order.getId(), JsonUtils.toJson(dstOrderBuilder.build()));
        }
        //5.判断数据库更新数量是否符合预期
        if(orderDao.updateOrder(dstOrderBuilder.build(), srcOrder) == 0)  return Response.error(OrderErrCode.ERROR_ORDER_UPDATE_NUM);
        //6.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_DRIVER_ARRIVE, driverArriveReq.getOrderId());
        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> driverPickup(DriverPickupReq driverPickupReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(driverPickupReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2. 判断订单是否满足driverPickup条件
        if(!canDriverPickup(order)) return Response.error(OrderErrCode.ERROR_DRIVER_PICKUP_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order.OrderBuilder dstOrderBuilder = Order.builder().status(OrderConstant.OrderStatus.DRIVER_PICKUPED)
                                                            .driverPickupTime(currentTime)
                                                            .statusUpdateTime(currentTime);
        Order srcOrder = Order.builder().id(order.getId()).status(order.getStatus()).storeId(order.getStoreId()).build();
        //4.订单表中骑手还没有接单或者还没有到店，则需要补偿
        if(order.getDriverAcceptTime() ==0 || order.getDriverArriveTime() == 0){
            //4.1同步骑手接单的数据
            if(order.getDriverAcceptTime() == 0) dstOrderBuilder.driverAcceptTime(driverPickupReq.getDriverAcceptTime());
            if(order.getDriverArriveTime() == 0) dstOrderBuilder.driverArriveTime(driverPickupReq.getDriverArriveTime());
            logger.info("driverPickup补偿骑手状态成功，id={}, dstOrder={}", order.getId(), JsonUtils.toJson(dstOrderBuilder.build()));
        }
        //5.判断数据库更新数量是否符合预期
        if(orderDao.updateOrder(dstOrderBuilder.build(), srcOrder) == 0)  return Response.error(OrderErrCode.ERROR_ORDER_UPDATE_NUM);
        //6.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_DRIVER_PICKUP, driverPickupReq.getOrderId());
        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> driverDeliver(DriverDeliverOrderReq driverDeliverOrderReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(driverDeliverOrderReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);

        //2. 判断订单是否满足deliver条件
        if(!canDeliver(order)) return Response.error(OrderErrCode.ERROR_DELIVER_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order.OrderBuilder dstOrderBuilder = Order.builder().status(OrderConstant.OrderStatus.DELIVERED)
                                                            .deliverTime(currentTime)
                                                            .statusUpdateTime(currentTime);
        Order srcOrder = Order.builder().id(order.getId()).storeId(order.getStoreId()).status(order.getStatus()).build();
        //4.订单表中骑手还没有接单或者还没有到店，则需要补偿
        if(order.getDriverAcceptTime() == 0 || order.getDriverArriveTime() == 0 || order.getDriverPickupTime() == 0){
            //4.1同步骑手接单的数据
            if(order.getDriverAcceptTime() == 0) dstOrderBuilder.driverAcceptTime(driverDeliverOrderReq.getDriverAcceptTime());
            if(order.getDriverArriveTime() == 0) dstOrderBuilder.driverArriveTime(driverDeliverOrderReq.getDriverArriveTime());
            if(order.getDriverPickupTime() == 0) dstOrderBuilder.driverPickupTime(driverDeliverOrderReq.getDriverPickupTime());
            logger.info("driverDeliver补偿骑手状态成功，id={}, dstOrder={}", order.getId(), JsonUtils.toJson(dstOrderBuilder.build()));
        }
        //5.判断数据库更新数量是否符合预期
        if(orderDao.updateOrder(dstOrderBuilder.build(), srcOrder) == 0)  return Response.error(OrderErrCode.ERROR_ORDER_UPDATE_NUM);
        //6.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_DRIVER_DELIVER, driverDeliverOrderReq.getOrderId());
        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<Void> cancelOrder(CancelOrderReq cancelOrderReq) {
        // 1.查询订单
        Order order = orderDao.getOrder(cancelOrderReq.getOrderId());
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);

        //2. 判断订单是否满足取消条件
        if(!canCancel(order)) return Response.error(OrderErrCode.ERROR_CANCEL_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        long currentTime = System.currentTimeMillis();
        Order dstOrder = Order.builder().status(OrderConstant.OrderStatus.CANCELED)
                                        .cancelTime(currentTime)
                                        .statusUpdateTime(currentTime)
                                        .build();
        Order srcOrder = Order.builder().id(order.getId()).status(order.getStatus()).storeId(order.getStoreId()).build();
        //4.判断数据库更新数量是否符合预期
        if(orderDao.updateOrder(dstOrder, srcOrder) == 0)  return Response.error(OrderErrCode.ERROR_ORDER_UPDATE_NUM);
        //5.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_CANCEL, cancelOrderReq.getOrderId());
        return Response.success(null);
    }

    @Override
    public Response<Void> completeOrder(Order order) {
        // 1.查询订单
        if(order == null) return Response.error(OrderErrCode.ORDER_NOT_FOUND);
        //2. 判断订单是否满足complete条件
        if(!canComplete(order))  return Response.error(OrderErrCode.ERROR_DELIVER_STATUS);
        //3.构成更新后的字段和更新条件并且执行更新
        Order dstOrder = Order.builder().status(OrderConstant.OrderStatus.COMPLETED)
                                        .completeTime(System.currentTimeMillis())
                                        .build();
        Order srcOrder = Order.builder().id(order.getId()).status(OrderConstant.OrderStatus.DELIVERED).build();
        //4.判断数据库更新数量是否符合预期
        if(orderDao.updateOrder(dstOrder, srcOrder) == 0)  return Response.error(OrderErrCode.ERROR_ORDER_UPDATE_NUM);
        //5.发送kafka事件
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_COMPLETE, order.getId());
        return Response.success(null);
    }

    @Override
    @RpcHandler(isRecordLog = false)
    public Response<QueryOrderByStoreRes> queryOrderByStore(QueryOrderByStoreReq queryOrderByStoreReq) {
        OrderStore.OrderStoreBuilder orderStoreBuilder = OrderStore.builder().storeId(queryOrderByStoreReq.getStoreId());
        if(queryOrderByStoreReq.getFilterType() == OrderConstant.QueryStoreFilterType.QUERY_NEW_ORDERS){
            orderStoreBuilder.isMerchantVisible(true);
            orderStoreBuilder.isCanceled(false);
            orderStoreBuilder.isMerchantConfirmed(false);
        }
        //查询size+1个订单 用来判断是否下一次用户是否还需要查询
        List<OrderStore> orderStores = orderDao.getOrderStores(orderStoreBuilder.build(), queryOrderByStoreReq.getLastId(), queryOrderByStoreReq.getSize()+1);
        //没有查到订单
        if(orderStores.size() == 0){
            return Response.success(QueryOrderByStoreRes.builder()
                                                .orders(Collections.emptyList())
                                                .hasMore(false)
                                                .lastId(0)
                                                .build());
        }
        boolean hasMore = orderStores.size() > queryOrderByStoreReq.getSize();
        orderStores = hasMore? orderStores.subList(0, orderStores.size()-1): orderStores;
        long lastId = orderStores.get(orderStores.size()-1).getOrderId();
        //根据订单id去订单表里批量查询订单信息
        List<Order> orders = new ArrayList<>(orderDao.getOrders(orderStores.stream().map(OrderStore::getOrderId).collect(Collectors.toList())).values());
        return Response.success(QueryOrderByStoreRes.builder()
                                                        .orders(orders)
                                                        .hasMore(hasMore)
                                                        .lastId(lastId)
                                                        .build());
    }

    @Override
    public Response<Void> confirmOrder(Order order){
        //1. 判断订单是否满足confirm条件
        if(!canConfirm(order)) return Response.error(OrderErrCode.ERROR_CONFIRM_STATUS);
        //2.构建更新字段和更新条件
        Order dstOrder = Order.builder().status(OrderConstant.OrderStatus.MERCHANT_CONFIRMED)
                                        .merchantConfirmTime(System.currentTimeMillis())
                                        .build();
        Order srcOrder = Order.builder().id(order.getId()).storeId(order.getStoreId()).status(order.getStatus()).isMerchantVisible(true).build();
        //3.更新订单失败直接返回错误
        if(orderDao.updateOrder(dstOrder, srcOrder) == 0) return Response.error(OrderErrCode.ERROR_CONFIRM_STATUS);
        //4.订单更新成功发送kafka消息
        sendOrderEvent(OrderConstant.KafkaEvent.ORDER_MERCHANT_CONFIRM, order.getId());
        //5.MFF模式下商家接单后需要调用运单接口通知运单派单
        if(!order.isDff()){
            Response<Long> deliveryResponse = deliveryService.createDelivery(buildDeliveryReq(order));
            if(!deliveryResponse.isSuccess())
                logger.warn("confirmOrder在MFF模式下创建运单失败,id={},code={}", order.getId(), deliveryResponse.getCode());
        }
        return Response.success(null);
    }

    private void sendOrderEvent(String eventType, long orderId){
        Order order = orderDao.getOrder(orderId);
        kafkaProducer.send(CommonConstant.KafkaTopic.ORDER_EVENT, eventType, String.valueOf(orderId), order);
    }

    private boolean canPay(Order order){
        if(order.getStatus() != OrderConstant.OrderStatus.CREATED){
            return false;
        }
        return true;
    }

    private boolean canDriverAccept(Order order){
        //1.dff模式上一个状态必须是approve
        if(order.isDff()) return order.getStatus() == OrderConstant.OrderStatus.APPROVED;
        //2.mff模式上一个状态必须是merchantConfirm
        return order.getStatus() == OrderConstant.OrderStatus.MERCHANT_CONFIRMED;
    }

    private boolean canDriverArrive(Order order){
        //1.dff模式上一个状态是approve(driveAccept可能暂时还没有同步)driverAccept或者merchantConfirm
        if(order.isDff()) return Arrays.asList(OrderConstant.OrderStatus.APPROVED,OrderConstant.OrderStatus.DRIVER_ACCEPTED,
                                               OrderConstant.OrderStatus.MERCHANT_CONFIRMED)
                                        .contains(order.getStatus());
        //2.mff模式上一个状态是merchantConfirm(driveAccept可能暂时还没有同步)或者driverAccept
        return  Arrays.asList(OrderConstant.OrderStatus.MERCHANT_CONFIRMED,OrderConstant.OrderStatus.DRIVER_ACCEPTED)
                      .contains(order.getStatus());
    }

    private boolean canDriverPickup(Order order){
        //1.dff模式上一个状态是driverArrive或者merchantConfirm(driverArrive可能暂时没有同步)
        if(order.isDff()) return Arrays.asList(OrderConstant.OrderStatus.MERCHANT_CONFIRMED,OrderConstant.OrderStatus.DRIVER_ARRIVED)
                                       .contains(order.getStatus());
        //2.mff模式上一个状态是merchantConfirm(driveAccept，driverArrive可能暂时还没有同步)或者driverAccept(driverArrive可能暂时还没有同步)或者driverArrive
        return  Arrays.asList(OrderConstant.OrderStatus.MERCHANT_CONFIRMED, OrderConstant.OrderStatus.DRIVER_ACCEPTED,
                              OrderConstant.OrderStatus.DRIVER_ARRIVED)
                .contains(order.getStatus());
    }

    private boolean canDeliver(Order order){
        //1.dff模式上一个状态是driverArrive或者driverPickup或者merchantConfirm(driverArrive可能暂时没有同步)
        if(order.isDff()) return Arrays.asList(OrderConstant.OrderStatus.DRIVER_ARRIVED,OrderConstant.OrderStatus.DRIVER_PICKUPED,
                                               OrderConstant.OrderStatus.MERCHANT_CONFIRMED)
                                       .contains(order.getStatus());
        //2.mff模式上一个状态是merchantConfirm(driveAccept，driverArrive, driverPickup可能暂时还没有同步)
        // 或者driverAccept(driverArrive,driverPickup可能暂时还没有同步)
        // 或者driverArrive(driverPickup可能暂时还没有同步) 或者 driverPickup
        return  Arrays.asList(OrderConstant.OrderStatus.MERCHANT_CONFIRMED, OrderConstant.OrderStatus.DRIVER_ACCEPTED,
                              OrderConstant.OrderStatus.DRIVER_ARRIVED, OrderConstant.OrderStatus.DRIVER_PICKUPED)
                      .contains(order.getStatus());
    }

    private boolean canComplete(Order order){
        return order.getStatus() == OrderConstant.OrderStatus.DELIVERED;
    }

    private boolean canConfirm(Order order){
        //判断订单是否对商家可见
        //1.MFF模式下订单的状态必须是Approve
        //2.DFF模式下订单的状态必须是driverAccept或者driverArrive状态
        return order.getIsMerchantVisible();
    }

    private boolean canApprove(Order order){
        if(order.getStatus() != OrderConstant.OrderStatus.PAID){
            return false;
        }
        return true;
    }

    private boolean canCancel(Order order){
        return !(order.getStatus() == OrderConstant.OrderStatus.CANCELED || order.getStatus() == OrderConstant.OrderStatus.DELIVERED
                || order.getStatus() ==OrderConstant.OrderStatus.COMPLETED);
    }

    private Order buildOrder(SubmitOrderReq submitOrderReq, List<CartItem> cartItems, Map<Long, StoreItem> itemMap){
        //生成分布式order id
        Response<Long> idRes = idGenService.genID(CommonConstant.IdServiceType.ID_ORDER_SERVICE_TYPE);
        if(!idRes.isSuccess()){
            throw BaseError.newBaseError(OrderErrCode.ERROR_GEN_ORDER_ID);
        }
        long currentTime = System.currentTimeMillis();
        Order newOrder = Order.builder()
                .id(idRes.getData() * CommonConstant.DbShardNum.ORDER + submitOrderReq.getBuyerId() % CommonConstant.DbShardNum.ORDER)
                .status(OrderConstant.OrderStatus.CREATED)
                .buyerId(submitOrderReq.getBuyerId())
                .storeId(submitOrderReq.getStoreId())
                .submitTime(currentTime)
                .deliveryAddress(submitOrderReq.getDeliveryAddress().getAddress())
                .deliveryName(submitOrderReq.getDeliveryAddress().getName())
                .deliveryPhone(submitOrderReq.getDeliveryAddress().getPhone())
                .deliveryLatitude(submitOrderReq.getDeliveryAddress().getLatitude())
                .deliveryLongitude(submitOrderReq.getDeliveryAddress().getLongitude())
                .deliveryDistance(100.0)
                .deliveryFee(5)
                .totalAmount(cartItems.stream()
                .mapToInt(cartItem -> cartItem.getQuantity() * itemMap.get(cartItem.getItemId()).getPrice()).sum())
                .statusUpdateTime(currentTime)
                .build();
        if(new Random().nextBoolean()) newOrder.setDff();
        if(new Random().nextBoolean()) newOrder.setAutoConfirm();
        if(new Random().nextBoolean()) newOrder.setOvertimeCancel();
        return newOrder;
    }

    private List<OrderItem> buildOrderItems(List<CartItem> cartItems, Order order, Map<Long, StoreItem> itemMap){
        Response<List<Long>> orderItemIdRes = idGenService.batchGenID(CommonConstant.IdServiceType.ID_ORDER_SERVICE_TYPE, cartItems.size());
        if(!orderItemIdRes.isSuccess()){
            throw BaseError.newBaseError(OrderErrCode.ERROR_GEN_ORDER_ITEM_IDS);
        }
        //批量生成分布式order item id
        List<OrderItem> orderItems = IntStream.range(0, cartItems.size())
                .mapToObj(index -> OrderItem.builder()
                        .id(orderItemIdRes.getData().get(index))
                        .orderId(order.getId())
                        .itemId(cartItems.get(index).getItemId())
                        .name(itemMap.get(cartItems.get(index).getItemId()).getName())
                        .quantity(cartItems.get(index).getQuantity())
                        .price(itemMap.get(cartItems.get(index).getItemId()).getPrice())
                        .build())
                .collect(Collectors.toList());
        return orderItems;
    }

    private CreateDeliveryReq buildDeliveryReq(Order order){
        return CreateDeliveryReq.builder()
                                .orderId(order.getId())
                                .storeId(order.getStoreId())
                                .deliveryName(order.getDeliveryName())
                                .deliveryPhone(order.getDeliveryPhone())
                                .deliveryAddress(order.getDeliveryAddress())
                                .deliveryLatitude(order.getDeliveryLatitude())
                                .deliveryLongitude(order.getDeliveryLongitude())
                                .isDff(order.isDff())
                                .build();
    }
}
