package com.cherish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cherish.entity.order.Order;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
