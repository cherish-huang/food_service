package com.cherish.entity.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddress implements Serializable{
    private String name;
    private String phone;
    private String address;
    private double longitude;
    private double latitude;
}
