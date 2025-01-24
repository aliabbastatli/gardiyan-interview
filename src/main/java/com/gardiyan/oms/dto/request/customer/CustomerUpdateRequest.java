package com.gardiyan.oms.dto.request.customer;

import lombok.Data;

@Data
public class CustomerUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
} 