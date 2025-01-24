package com.gardiyan.oms.dto.request.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
} 