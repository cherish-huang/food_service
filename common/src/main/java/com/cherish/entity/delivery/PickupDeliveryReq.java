package com.cherish.entity.delivery;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class PickupDeliveryReq implements Serializable {
    private long deliveryOrderId;
}
