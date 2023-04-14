package com.cherish.entity.order;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class QueryOrderByStoreRes implements Serializable {
    private List<Order> orders;
    private long lastId;
    private boolean hasMore;
}
