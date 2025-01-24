package com.gardiyan.oms.dto.request.order;

import lombok.Data;
import java.util.UUID;

@Data
public class OrderItemRequest {
    private UUID productId;
    private int quantity;
} 