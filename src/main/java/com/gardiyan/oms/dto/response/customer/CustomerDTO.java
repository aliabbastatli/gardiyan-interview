package com.gardiyan.oms.dto.response.customer;

import lombok.Data;
import java.util.UUID;

@Data
public class CustomerDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
} 