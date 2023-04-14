package com.cherish.entity.order;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class QueryOrderByStoreReq implements Serializable {
    private int filterType;
    private long storeId;
    private long lastId;
    private Integer size;
}
