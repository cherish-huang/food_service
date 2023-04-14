package com.cherish.entity.order;

import com.cherish.entity.cart.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitOrderReq implements Serializable{
    private Long buyerId;
    private Long StoreId;
    private DeliveryAddress deliveryAddress;
    private List<CartItem> cartItems;
}
