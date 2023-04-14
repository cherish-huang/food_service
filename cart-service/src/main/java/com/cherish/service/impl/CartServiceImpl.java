package com.cherish.service.impl;

import com.cherish.component.rpc.RpcHandler;
import com.cherish.constant.CommonConstant;
import com.cherish.dao.CartDao;
import com.cherish.entity.cart.CartItem;
import com.cherish.entity.rpc.Response;
import com.cherish.error.CartErrCode;
import com.cherish.service.cart.CartService;
import com.cherish.service.idgen.IDGenService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class CartServiceImpl implements CartService {

    @Autowired
    private CartDao cartDao;

    @DubboReference(check = false)
    private IDGenService idGenService;

    @Override
    @RpcHandler
    public Response<Void> addCartItem(CartItem cartItem) {
        //1.根据买家id,商家id,菜品id查询cartItem
        CartItem oldCartItem = cartDao.queryCartItem(cartItem.getBuyerId(), cartItem.getStoreId(), cartItem.getItemId());
        //2.购物车里有该商品,需要将购物车原有的记录和新增的记录进行合并,然后更新数据库
        if(oldCartItem != null){
            int newQuantity = Math.max(0, oldCartItem.getQuantity() + cartItem.getQuantity());
            //2.2加减购后菜品数量不为0则更新db
            if(newQuantity > 0){
                CartItem dstCartItem = CartItem.builder().quantity(newQuantity).build();
                CartItem srcCartItem = CartItem.builder().id(oldCartItem.getId()).quantity(oldCartItem.getQuantity()).build();
                if(cartDao.updateCartItem(dstCartItem, srcCartItem) == 0) return Response.error(CartErrCode.ERROR_UPDATE_DB);
            }
            //2.1减购后菜品没有则删除cartItem, 删除失败返回错误
            else if(cartDao.deleteCartItem(oldCartItem.getId()) == 0) return Response.error(CartErrCode.ERROR_DELETE_DB);
            return Response.success(null);
        }
        //3.如果菜品不存在进行减购,直接返回异常
        if(cartItem.getQuantity() < 0) return Response.error(CartErrCode.ERROR_REDUCE_CART);
        //4.生成购物车id
        Response<Long> genIDRes = idGenService.genID(CommonConstant.IdServiceType.ID_CART_SERVICE_TYPE);
        if(!genIDRes.isSuccess()) return Response.error(CartErrCode.ERROR_GEN_ORDER_ID);
        //5.购物车原本没有该商品，直接往数据库插入新的记录
        CartItem newCartItem = CartItem.builder()
                                .id(genIDRes.getData() * CommonConstant.DbShardNum.CART_ITEM + cartItem.getBuyerId() % CommonConstant.DbShardNum.CART_ITEM)
                                .itemId(cartItem.getItemId())
                                .storeId(cartItem.getStoreId())
                                .buyerId(cartItem.getBuyerId())
                                .quantity(cartItem.getQuantity())
                                .build();
        if(cartDao.addCartItem(newCartItem) == 0) return Response.error(CartErrCode.ERROR_INSERT_DB);
        return Response.success(null);
    }

    @Override
    @RpcHandler
    public Response<List<CartItem>> getCartItems(Long buyerId, Long storeId) {
        return Response.success(cartDao.queryCartItems(buyerId, storeId));
    }

    @Override
    @RpcHandler
    public Response<Void> emptyCartItem(Long buyerId, Long storeId) {
        if(cartDao.deleteCartItem(buyerId, storeId) == 0) return Response.error(CartErrCode.ERROR_EMPTY_DB);
        return Response.success(null);
    }
}
