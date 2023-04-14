package com.cherish.entity.delivery;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cherish.constant.CommonConstant;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@TableName(CommonConstant.DbShardName.DRIVER_ORDER)
public class DriverOrder implements Serializable {
    @TableId
    private Long id;
    private Integer status;
    private Long driverId;
    private Double deliveryFee;
    private Long deliveryOrderId;
    private Long createTime;
}
