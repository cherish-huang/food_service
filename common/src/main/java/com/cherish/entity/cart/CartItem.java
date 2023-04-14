package com.cherish.entity.cart;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cherish.constant.CommonConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(CommonConstant.DbShardName.CART_ITEM)
public class CartItem implements Serializable {
    @Id
    private Long id;
    private Long buyerId;
    private Long storeId;
    private Long itemId;
    private Integer quantity;
}
