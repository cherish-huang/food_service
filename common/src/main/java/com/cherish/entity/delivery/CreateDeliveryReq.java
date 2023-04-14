package com.cherish.entity.delivery;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class CreateDeliveryReq implements Serializable {
    private long orderId;
    private long storeId;
    private String deliveryName;
    private String deliveryPhone;
    private double deliveryLatitude;
    private double deliveryLongitude;
    private String deliveryAddress;
    private boolean isDff;
}
