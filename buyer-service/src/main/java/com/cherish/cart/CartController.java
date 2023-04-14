package com.cherish.cart;

import com.cherish.entity.cart.CartItem;
import com.cherish.entity.rpc.Response;
import com.cherish.service.cart.CartService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @DubboReference(check = false)
    private CartService cartService;

    @PostMapping("/cart/addCartItem")
    public Response<Void> addCartItem(@RequestBody CartItem cartItem){
       return cartService.addCartItem(cartItem);
    }

    @GetMapping("/cart/getCartItems/{buyerId}/{storeId}")
    public Response<List<CartItem>> getCartItems(@PathVariable long buyerId, @PathVariable long storeId){
        return cartService.getCartItems(buyerId, storeId);
    }
}
