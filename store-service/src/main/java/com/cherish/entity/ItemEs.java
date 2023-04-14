package com.cherish.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
@Data
@Builder
@Document(indexName = "items")
public class ItemEs {
    @Id
    private String id;
    private String name;
    private String price;
    private String storeId;
}
