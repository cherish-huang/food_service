package com.cherish.consumer.cron;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.component.redis.RedisManager;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.DeliveryConstant;
import com.cherish.dao.DeliveryDao;
import com.cherish.entity.delivery.AcceptDeliveryReq;
import com.cherish.entity.delivery.CancelDeliveryReq;
import com.cherish.entity.delivery.DeliveryOrder;
import com.cherish.entity.rpc.Response;
import com.cherish.service.delivery.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class DeliveryStatusCron {

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private DeliveryDao deliveryDao;

    @Autowired
    private DeliveryService deliveryService;

    private final int batchNum = 100;

    Logger logger = LoggerFactory.getLogger(DeliveryStatusCron.class);

    /**
     * 分配骑手接单定时器
     */
    @Scheduled(cron = "0 */3 * * * ?")
    public void assignDelivery(){
        List<String> drivers = redisManager.srandommembers(DeliveryConstant.RedisPrefix.AVAILABLE_DRIVERS, batchNum);
        List<Long> driverIds = drivers.stream().map(Long::parseLong).collect(Collectors.toList());
        if(driverIds.isEmpty()){
            logger.warn("assignDelivery 没有可用骑手....");
            return;
        }
        for(int shardId=0; shardId < CommonConstant.DbShardNum.DELIVERY_ORDER; shardId++){
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                LambdaQueryWrapper<DeliveryOrder> queryWrapper = Wrappers.<DeliveryOrder>lambdaQuery()
                        .eq(DeliveryOrder::getStatus, DeliveryConstant.DeliveryOrderStatus.CREATED)
                        .gt(DeliveryOrder::getId, lastId);
                List<DeliveryOrder> deliveryOrders = deliveryDao.getDeliveryOrders(queryWrapper, shardId, batchNum);
                if (deliveryOrders.size() < batchNum) flag = false;
                if (deliveryOrders.isEmpty()) break;
                lastId = deliveryOrders.get(deliveryOrders.size() - 1).getId();
                for (DeliveryOrder deliveryOrder : deliveryOrders) {
                    long driverId = driverIds.get(new Random().nextInt(driverIds.size()));
                    Response<Void> response = deliveryService.acceptDelivery(AcceptDeliveryReq.builder().deliveryOrderId(deliveryOrder.getId()).driverId(driverId).build());
                    if(response.isSuccess()) logger.info("assignDelivery定时器acceptDelivery成功,id={},orderId={},driverId={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), driverId);
                    else logger.error("assignDelivery定时器acceptDelivery失败,id={},orderId={},driverId={},code={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), driverId, response.getCode());
                }
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void assignTimeoutDelivery(){
        for(int shardId=0; shardId < CommonConstant.DbShardNum.DELIVERY_ORDER; shardId++){
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                // 10分钟没有分配到骑手，取消运单
                LambdaQueryWrapper<DeliveryOrder> queryWrapper = Wrappers.<DeliveryOrder>lambdaQuery()
                        .eq(DeliveryOrder::getStatus, DeliveryConstant.DeliveryOrderStatus.CREATED)
                        .lt(DeliveryOrder::getCreateTime, System.currentTimeMillis() - 10 * 60 * 1000)
                        .gt(DeliveryOrder::getId, lastId);
                List<DeliveryOrder> deliveryOrders = deliveryDao.getDeliveryOrders(queryWrapper, shardId, batchNum);
                if (deliveryOrders.size() < batchNum) flag = false;
                if (deliveryOrders.isEmpty()) break;
                lastId = deliveryOrders.get(deliveryOrders.size() - 1).getId();
                for (DeliveryOrder deliveryOrder : deliveryOrders) {
                    Response<Void> response = deliveryService.cancelDelivery(CancelDeliveryReq.builder().deliveryOrderId(deliveryOrder.getId())
                                                                                                        .cancelSource(DeliveryConstant.CancelSource.SYSTEM)
                                                                                                        .cancelReason(DeliveryConstant.CancelReason.ASSIGN_TIMEOUT).build());
                    if(response.isSuccess()) logger.info("assignTimeoutDelivery定时器cancelDelivery成功,id={},orderId={}", deliveryOrder.getId(), deliveryOrder.getOrderId());
                    else logger.error("assignTimeoutDelivery定时器cancelDelivery失败,id={},orderId={},code={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), response.getCode());
                }
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void deliverTimeoutDelivery(){
        for(int shardId=0; shardId < CommonConstant.DbShardNum.DELIVERY_ORDER; shardId++){
            long lastId = 0;
            boolean flag = true;
            while (flag) {
                // 1小时没有送达，取消运单
                LambdaQueryWrapper<DeliveryOrder> queryWrapper = Wrappers.<DeliveryOrder>lambdaQuery()
                        .in(DeliveryOrder::getStatus, Arrays.asList(DeliveryConstant.DeliveryOrderStatus.CONFIRMED,
                                                                    DeliveryConstant.DeliveryOrderStatus.ARRIVED,
                                                                    DeliveryConstant.DeliveryOrderStatus.PICKED))
                        .lt(DeliveryOrder::getAssignTime, System.currentTimeMillis() - 60 * 60 * 1000)
                        .gt(DeliveryOrder::getId, lastId);
                List<DeliveryOrder> deliveryOrders = deliveryDao.getDeliveryOrders(queryWrapper, shardId, batchNum);
                if (deliveryOrders.size() < batchNum) flag = false;
                if (deliveryOrders.isEmpty()) break;
                lastId = deliveryOrders.get(deliveryOrders.size() - 1).getId();
                for (DeliveryOrder deliveryOrder : deliveryOrders) {
                    Response<Void> response = deliveryService.cancelDelivery(CancelDeliveryReq.builder().deliveryOrderId(deliveryOrder.getId())
                                                                                                .cancelSource(DeliveryConstant.CancelSource.SYSTEM)
                                                                                                .cancelReason(DeliveryConstant.CancelReason.DELIVER_TIMEOUT).build());
                    if(response.isSuccess()) logger.info("deliverTimeoutDelivery定时器cancelDelivery成功,id={},orderId={}", deliveryOrder.getId(), deliveryOrder.getOrderId());
                    else logger.error("deliverTimeoutDelivery定时器cancelDelivery失败,id={},orderId={},code={}", deliveryOrder.getId(), deliveryOrder.getOrderId(), response.getCode());
                }
            }
        }
    }
}
