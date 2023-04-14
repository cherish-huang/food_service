package com.cherish.entity.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cherish.constant.CommonConstant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName(CommonConstant.DbShardName.ORDER_ITEM)
public class OrderItem {
    @TableId
    private Long id;
    private Long orderId;
    private Long itemId;
    private String name;
    private Integer quantity;
    private Integer price;
}
