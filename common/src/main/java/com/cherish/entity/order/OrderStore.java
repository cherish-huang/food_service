package com.cherish.entity.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cherish.constant.CommonConstant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName(CommonConstant.DbShardName.ORDER_STORE)
public class OrderStore {
    @TableId
    private Long orderId;
    private Long storeId;
    private Integer orderStatus;
    private Boolean isMerchantVisible;
    private Boolean isMerchantConfirmed;
    private Boolean isPickedUp;
    private Boolean isCanceled;
}
