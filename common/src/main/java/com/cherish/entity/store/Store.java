package com.cherish.entity.store;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_store")
public class Store implements Serializable {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double Longitude;
}
