package com.cherish.entity.delivery;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelDeliveryReq {
    private long deliveryOrderId;
    private int cancelSource;
    private int cancelReason;
}
