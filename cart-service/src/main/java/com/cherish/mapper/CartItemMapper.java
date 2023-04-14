package com.cherish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cherish.entity.cart.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
