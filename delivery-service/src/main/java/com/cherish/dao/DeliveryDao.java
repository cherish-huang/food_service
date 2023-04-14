package com.cherish.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.component.mysql.ShardTableNameHandler;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.delivery.DriverOrder;
import com.cherish.entity.order.Order;
import com.cherish.mapper.DeliveryOrderMapper;
import com.cherish.mapper.DriverOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DeliveryDao {

    @Autowired
    private DeliveryOrderMapper deliveryOrderMapper;

    @Autowired
    private DriverOrderMapper driverOrderMapper;

    public int insertDeliveryOrder(DeliveryOrder deliveryOrder){
        ShardTableNameHandler.setCurrentShardId((int) (deliveryOrder.getId() % CommonConstant.DbShardNum.DELIVERY_ORDER));
        return deliveryOrderMapper.insert(deliveryOrder);
    }

    public List<DeliveryOrder> getDeliveryOrders(LambdaQueryWrapper<DeliveryOrder> queryWrapper, int shardId, int size){
        ShardTableNameHandler.setCurrentShardId(shardId);
        queryWrapper.last("limit " + size);
        return deliveryOrderMapper.selectList(queryWrapper);
    }

    public DeliveryOrder getDeliveryOrder(long id){
        ShardTableNameHandler.setCurrentShardId((int) (id % CommonConstant.DbShardNum.DELIVERY_ORDER));
        return deliveryOrderMapper.selectById(id);
    }

    public List<DeliveryOrder> getDeliveryOrders(List<Long> ids){
        List<DeliveryOrder> deliveryOrders = new ArrayList<>();
        Map<Integer, List<Long>> shardMap = new HashMap<>();
        for(long id : ids){
            int shardId = (int) (id % CommonConstant.DbShardNum.DELIVERY_ORDER);
            List<Long> shardIds = !shardMap.containsKey(shardId) ? new ArrayList<>() : shardMap.get(shardId);
            shardIds.add(id);
            shardMap.put(shardId, shardIds);
        }
        shardMap.forEach((shardId, shardIds) -> {
            ShardTableNameHandler.setCurrentShardId(shardId);
            deliveryOrders.addAll(deliveryOrderMapper.selectBatchIds(shardIds));
        });
        return deliveryOrders;
    }

    public List<DeliveryOrder> getDeliveryOrdersByOrderIds(List<Long> orderIds){
        List<DeliveryOrder> deliveryOrders = new ArrayList<>();
        Map<Integer, List<Long>> shardMap = new HashMap<>();
        for(long id : orderIds){
            int shardId = (int) (id % CommonConstant.DbShardNum.DELIVERY_ORDER);
            List<Long> shardIds = !shardMap.containsKey(shardId) ? new ArrayList<>() : shardMap.get(shardId);
            shardIds.add(id);
            shardMap.put(shardId, shardIds);
        }
        shardMap.forEach((shardId, shardIds) -> {
            ShardTableNameHandler.setCurrentShardId(shardId);
            deliveryOrders.addAll(deliveryOrderMapper.selectList(Wrappers.<DeliveryOrder>lambdaQuery().in(DeliveryOrder::getOrderId, shardIds)));
        });
        return deliveryOrders;
    }

    public int updateDeliveryOrder(DeliveryOrder dstDeliveryOrder, DeliveryOrder srcDeliveryOrder){
        ShardTableNameHandler.setCurrentShardId((int) (srcDeliveryOrder.getId() % CommonConstant.DbShardNum.DELIVERY_ORDER));
        return deliveryOrderMapper.update(dstDeliveryOrder, Wrappers.lambdaUpdate(srcDeliveryOrder));
    }

    public DriverOrder getDriverOrder(long id){
        ShardTableNameHandler.setCurrentShardId((int) (id % CommonConstant.DbShardNum.DRIVER_ORDER));
        return driverOrderMapper.selectById(id);
    }

    @Transactional
    public int insertDriverAndUpdateDeliveryOrder(DriverOrder driverOrder, DeliveryOrder srcDeliveryOrder, DeliveryOrder dstDeliveryOrder){
        ShardTableNameHandler.setCurrentShardId((int) (srcDeliveryOrder.getId() % CommonConstant.DbShardNum.DELIVERY_ORDER));
        if(deliveryOrderMapper.update(dstDeliveryOrder, Wrappers.lambdaUpdate(srcDeliveryOrder)) == 0) return 0;
        ShardTableNameHandler.setCurrentShardId((int) (driverOrder.getId() % CommonConstant.DbShardNum.DRIVER_ORDER));
        return driverOrderMapper.insert(driverOrder);
    }

    public List<DeliveryOrder> queryDeliveries(LambdaQueryWrapper<DeliveryOrder> queryWrapper, int shardId, int size){
        queryWrapper.last("limit " +size);
        ShardTableNameHandler.setCurrentShardId(shardId);
        return deliveryOrderMapper.selectList(queryWrapper);
    }

    @Transactional
    public int updateDeliveryDriverOrder(DeliveryOrder dstDeliveryOrder, DeliveryOrder srcDeliveryOrder,
                                         DriverOrder dstDriverOrder, DriverOrder srcDriverOrder){
        ShardTableNameHandler.setCurrentShardId((int) (srcDeliveryOrder.getId() % CommonConstant.DbShardNum.DELIVERY_ORDER));
        if(deliveryOrderMapper.update(dstDeliveryOrder, Wrappers.lambdaUpdate(srcDeliveryOrder)) == 0) return 0;

        ShardTableNameHandler.setCurrentShardId((int) (srcDriverOrder.getId() % CommonConstant.DbShardNum.DRIVER_ORDER));
        return driverOrderMapper.update(dstDriverOrder, Wrappers.lambdaUpdate(srcDriverOrder));
    }
}
