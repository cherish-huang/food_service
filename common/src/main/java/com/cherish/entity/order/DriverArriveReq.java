package com.cherish.entity.order;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class DriverArriveReq implements Serializable {
    private long orderId;
    private Long driverAcceptTime; //补偿是才用到
}
