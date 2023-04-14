package com.cherish.entity.order;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class CancelOrderReq implements Serializable {
    private long orderId;
    private int cancelSource;
    private int cancelReason;
}
