package com.cherish.entity.order;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class DriverPickupReq implements Serializable {
    private long orderId;
    private Long driverAcceptTime; //补偿时填充
    private Long driverArriveTime; //补偿时填充
}
