package com.cherish.entity.delivery;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class QueryDeliveryOrderRes  implements Serializable{
    List<DeliveryOrder> deliveryOrders;
}
