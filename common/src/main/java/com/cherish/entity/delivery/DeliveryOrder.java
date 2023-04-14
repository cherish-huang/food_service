package com.cherish.entity.delivery;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.DeliveryConstant;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@TableName(CommonConstant.DbShardName.DELIVERY_ORDER)
public class DeliveryOrder implements Serializable {
    @TableId
    private Long id;
    private Integer status;
    private Long orderId;
    private Long driverId;
    private Long driverOrderId;
    private Long flag;
    private String deliveryName;
    private String deliveryPhone;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryAddress;
    private Double storeLatitude;
    private Double storeLongitude;
    private String storeAddress;

    private Long assignTime;
    private Long arriveTime;
    private Long pickupTime;
    private Long completeTime;
    private Long createTime;
    private Long cancelTime;
    private Integer cancelSource;
    private Integer cancelReason;

    public boolean isDff(){
        return ((flag == null? 0: flag) >> DeliveryConstant.FlagBit.DFF_BIT & 1) == 1;
    }

    public void setDff(){
        flag = (flag == null? 0: flag) | 1 << DeliveryConstant.FlagBit.DFF_BIT;
    }
}
