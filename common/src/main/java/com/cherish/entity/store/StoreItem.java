package com.cherish.entity.store;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@TableName("t_store_item")
public class StoreItem implements Serializable {
    private Long id;
    private String name;
    private Integer price;
    private Long storeId;
}
