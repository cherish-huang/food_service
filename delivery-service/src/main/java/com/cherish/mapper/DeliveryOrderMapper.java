package com.cherish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cherish.entity.delivery.DeliveryOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeliveryOrderMapper extends BaseMapper<DeliveryOrder> {
}
