package com.cherish.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.component.mysql.ShardTableNameHandler;
import com.cherish.component.redis.RedisManager;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.order.Order;
import com.cherish.entity.order.OrderItem;
import com.cherish.entity.order.OrderStore;
import com.cherish.mapper.OrderItemMapper;
import com.cherish.mapper.OrderMapper;
import com.cherish.mapper.OrderStoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class OrderDao {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStoreMapper orderStoreMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisManager redisManager;

    @Transactional
    public int insertOrder(Order order, List<OrderItem> orderItems){
        int effectRows = this.insertOrder(order);
        this.insertOrderStore(order);
        this.insertOrderItems(orderItems);
        return effectRows;
    }

    public Order getOrder(long orderId){
        String orderKey = String.format(OrderConstant.RedisPrefix.ORDER_BASIC_INFO, orderId);
        Order resultObj = redisManager.get(orderKey, Order.class);
        if(resultObj != null) return resultObj;
        ShardTableNameHandler.setCurrentShardId((int) (orderId % CommonConstant.DbShardNum.ORDER));
        Order order = orderMapper.selectById(orderId);
        if(order != null){
            redisManager.set(orderKey, order);
        }
        return order;
    }

    /**
     * 通过orderIds 批量获取订单
     * @param orderIds
     * @return Map key=订单id, value=订单对象
     */
    public Map<Long, Order> getOrders(List<Long> orderIds){
        //1. 先去redis批量查询订单
        Map<Long, Order> cacheOrders = redisManager.mget(OrderConstant.RedisPrefix.ORDER_BASIC_INFO, orderIds, Order.class);
        //2. 过滤出redis不存在的订单
        List<Long> missCacheOrderIds = orderIds.stream().filter(orderId -> cacheOrders.containsKey(orderId)).collect(Collectors.toList());
        Map<Long, Order> missCacheOrders = new HashMap<>();
        Map<Integer, List<Long>> shardMap = new HashMap<>();
        //3.将redis不存在的订单映射成shardId -> orderIds
        for(long orderId : missCacheOrderIds){
            int shardId = (int) (orderId % CommonConstant.DbShardNum.ORDER);
            List<Long> shardOrderIds = !shardMap.containsKey(shardId) ? new ArrayList<>() : shardMap.get(shardId);
            shardOrderIds.add(orderId);
            shardMap.put(shardId, shardOrderIds);
        }
        //4.将对应的分表中批量查询订单，放入missCacheOrders中
        shardMap.forEach((shardId, shardOrderIds) -> {
            ShardTableNameHandler.setCurrentShardId(shardId);
            List<Order> shardOrders = orderMapper.selectBatchIds(orderIds);
            shardOrders.stream().forEach(shardOrder -> missCacheOrders.put(shardOrder.getId(), shardOrder));
        });
        //5.将missCacheOrders批量缓存到redis中
        redisManager.mset(OrderConstant.RedisPrefix.ORDER_BASIC_INFO, missCacheOrders);
        //6.将cacheOrders和missCacheOrders进行合并返回
        cacheOrders.putAll(missCacheOrders);
        return cacheOrders;
    }

    public List<Order> getOrders(LambdaQueryWrapper<Order> queryWrapper, int shardId, int size){
        ShardTableNameHandler.setCurrentShardId(shardId);
        queryWrapper.last("limit " + size);
        return orderMapper.selectList(queryWrapper);
    }

    @Transactional
    public int updateOrder(Order updateOrder, Order condition){
        if (condition.getId() == null) return 0;
        ShardTableNameHandler.setCurrentShardId((int) (condition.getId() % CommonConstant.DbShardNum.ORDER));
        if(orderMapper.update(updateOrder, Wrappers.lambdaUpdate(condition)) == 0) return 0;
        this.updateOrderStore(updateOrder, condition);
        redisManager.del(String.format(OrderConstant.RedisPrefix.ORDER_BASIC_INFO, condition.getId()));
        return 1;
    }

    private int insertOrder(Order order){
        ShardTableNameHandler.setCurrentShardId((int) (order.getId() % CommonConstant.DbShardNum.ORDER));
        return orderMapper.insert(order);
    }

    private void insertOrderStore(Order order){
        ShardTableNameHandler.setCurrentShardId((int) (order.getStoreId() % CommonConstant.DbShardNum.ORDER_STORE));
        OrderStore orderStore = OrderStore.builder()
                .orderId(order.getId())
                .storeId(order.getStoreId())
                .orderStatus(order.getStatus())
                .isCanceled(false)
                .isMerchantConfirmed(false)
                .isPickedUp(false)
                .isMerchantVisible(false)
                .build();
        orderStoreMapper.insert(orderStore);
    }

    private int insertOrderItems(List<OrderItem> orderItems) {
        ShardTableNameHandler.setCurrentShardId((int) (orderItems.get(0).getOrderId() % CommonConstant.DbShardNum.ORDER_ITEM));
        for(OrderItem orderItem: orderItems){
            orderItemMapper.insert(orderItem);
        }
        return orderItems.size();
    }

    public List<OrderStore> getOrderStores(OrderStore orderStore, long lastId, int size){
        ShardTableNameHandler.setCurrentShardId((int) (orderStore.getStoreId() % CommonConstant.DbShardNum.ORDER_STORE));
        return orderStoreMapper.selectList(Wrappers.lambdaQuery(orderStore)
                                                   .gt(OrderStore::getOrderId, lastId)
                                                   .orderByAsc(OrderStore::getOrderId)
                                                   .last("limit " + size));
    }

    private int updateOrderStore(Order updateOrder, Order condition){
        Long storeId = condition.getStoreId();
        if(storeId == null){
            Order order = this.getOrder(condition.getId());
            storeId = order.getStoreId();
        }

        boolean isNeedUpdate = false;
        OrderStore.OrderStoreBuilder builder = OrderStore.builder().orderId(condition.getId());
        if(updateOrder.getStatus() != null){
            builder.orderStatus(updateOrder.getStatus());
            isNeedUpdate = true;
        }

        if(updateOrder.getIsMerchantVisible() != null){
            builder.isMerchantVisible(updateOrder.getIsMerchantVisible());
            isNeedUpdate = true;
        }

        if(updateOrder.getMerchantConfirmTime() != null){
            builder.isMerchantConfirmed(true);
            isNeedUpdate = true;
        }

        if(updateOrder.getDriverPickupTime() != null){
            builder.isPickedUp(true);
            isNeedUpdate = true;
        }

        if(updateOrder.getCancelTime() != null){
            builder.isCanceled(true);
            isNeedUpdate = true;
        }
        if(!isNeedUpdate) return 0;
        ShardTableNameHandler.setCurrentShardId((int) (storeId % CommonConstant.DbShardNum.ORDER_STORE));
        return orderStoreMapper.updateById(builder.build());
    }
}
