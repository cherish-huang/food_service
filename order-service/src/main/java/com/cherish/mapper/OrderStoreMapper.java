package com.cherish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cherish.entity.order.OrderStore;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderStoreMapper extends BaseMapper<OrderStore> {
}
