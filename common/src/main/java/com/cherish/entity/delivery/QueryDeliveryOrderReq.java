package com.cherish.entity.delivery;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class QueryDeliveryOrderReq implements Serializable {
    private int filterType;
    private int size;
}
