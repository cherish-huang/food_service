package com.cherish.service.cart;

import com.cherish.entity.cart.CartItem;
import com.cherish.entity.rpc.Response;

import java.util.List;

public interface CartService {
    Response<Void> addCartItem(CartItem cartItem);
    Response<List<CartItem>> getCartItems(Long buyerId, Long storeId);

    Response<Void> emptyCartItem(Long buyerId, Long storeId);
}
