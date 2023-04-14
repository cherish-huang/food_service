package com.cherish.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cherish.component.mysql.ShardTableNameHandler;
import com.cherish.constant.CommonConstant;
import com.cherish.entity.cart.CartItem;
import com.cherish.mapper.CartItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartDao{

    @Autowired
    private CartItemMapper cartItemMapper;

    /**
     * 根据买家id，商家id，商品id查询购物车db
     * @param buyerId
     * @param storeId
     * @param itemId
     * @return
     */
    public CartItem queryCartItem(long buyerId, long storeId, long itemId){
        // t_cart_item db id的生成规则是 雪花算法生成的id *DbShardNum.CART_ITEM + buyerId % DbShardNum.CART_ITEM
        // 所以可以直接根据buyerId 当作db主键计算分表
        ShardTableNameHandler.setCurrentShardId((int) (buyerId % CommonConstant.DbShardNum.CART_ITEM));
        return cartItemMapper.selectOne(
                Wrappers.<CartItem>lambdaQuery()
                        .eq(CartItem::getBuyerId, buyerId)
                        .eq(CartItem::getStoreId, storeId)
                        .eq(CartItem::getItemId, itemId)
                );
    }

    /**
     * 更新购物车
     * @param dstCartItem
     * @param srcCartItem
     * @return
     */
    public int updateCartItem(CartItem dstCartItem, CartItem srcCartItem){
        ShardTableNameHandler.setCurrentShardId((int) (srcCartItem.getId() % CommonConstant.DbShardNum.CART_ITEM));
        return cartItemMapper.update(dstCartItem, Wrappers.lambdaUpdate(srcCartItem));
    }

    /**
     * 添加购物车
     * @param cartItem
     * @return
     */
    public int addCartItem(CartItem cartItem){
        ShardTableNameHandler.setCurrentShardId((int) (cartItem.getId() % CommonConstant.DbShardNum.CART_ITEM));
        return cartItemMapper.insert(cartItem);
    }

    public int deleteCartItem(long cartItemId){
        ShardTableNameHandler.setCurrentShardId((int) (cartItemId % CommonConstant.DbShardNum.CART_ITEM));
        return cartItemMapper.deleteById(cartItemId);
    }

    /**
     * 查询用户在某个商铺的购物车
     * @param buyerId
     * @param storeId
     * @return
     */
    public List<CartItem> queryCartItems(long buyerId, long storeId){
        ShardTableNameHandler.setCurrentShardId((int) (buyerId % CommonConstant.DbShardNum.CART_ITEM));
        return cartItemMapper.selectList(
                Wrappers.<CartItem>lambdaQuery()
                        .eq(CartItem::getBuyerId, buyerId)
                        .eq(CartItem::getStoreId, storeId)
                );
    }

    /**
     * 清空用户在某个商铺的购物车
     * @param buyerId
     * @param storeId
     * @return
     */
    public int deleteCartItem(long buyerId, long storeId){
        ShardTableNameHandler.setCurrentShardId((int) (buyerId % CommonConstant.DbShardNum.CART_ITEM));
        return cartItemMapper.delete(
                Wrappers.<CartItem>lambdaQuery()
                        .eq(CartItem::getBuyerId, buyerId)
                        .eq(CartItem::getStoreId, storeId)
                );
    }
}
