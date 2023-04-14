package com.cherish.entity.order;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MerchantConfirmReq implements Serializable {
    private long orderId;
}
