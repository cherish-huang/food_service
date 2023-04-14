package com.cherish.entity.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cherish.constant.CommonConstant;
import com.cherish.constant.OrderConstant;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@TableName(CommonConstant.DbShardName.ORDER)
public class Order implements Serializable {

    @TableId
    private Long id;
    private Integer status;
    private Long buyerId;
    private Long storeId;
    private Long flag;
    private Integer totalAmount;
    private Integer deliveryFee;
    private String deliveryName;
    private String deliveryPhone;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryAddress;
    private Double deliveryDistance;
    private Boolean isMerchantVisible;
    private Long merchantDeadline;
    private Long submitTime;
    private Long payTime;
    private Long approveTime;
    private Long merchantConfirmTime;
    private Long driverAcceptTime;
    private Long driverArriveTime;
    private Long driverPickupTime;
    private Long deliverTime;
    private Long completeTime;
    private Long cancelTime;
    private Integer cancelSource;
    private Integer cancelReason;
    private Long statusUpdateTime;

    public boolean isDff(){
        return ((flag == null? 0 :flag) >> OrderConstant.FlagBit.DFF_BIT & 1) == 1;
    }

    public boolean isAutoConfirm(){
        return ((flag == null? 0 :flag) >> OrderConstant.FlagBit.AUTO_CONFIRM_BIT & 1) == 1;
    }

    public boolean isOvertimeCancel(){
        return ((flag == null? 0 :flag) >> OrderConstant.FlagBit.MERCHANT_OVERTIME_BIT & 1) == 1;
    }

    public void setDff(){
        flag = (flag == null? 0: flag) | 1 << OrderConstant.FlagBit.DFF_BIT;
    }

    public void setAutoConfirm(){
        flag = (flag == null? 0: flag) | 1 << OrderConstant.FlagBit.AUTO_CONFIRM_BIT;
    }

    public void setOvertimeCancel(){
        flag = (flag == null? 0: flag) | 1 << OrderConstant.FlagBit.MERCHANT_OVERTIME_BIT;
    }
}
