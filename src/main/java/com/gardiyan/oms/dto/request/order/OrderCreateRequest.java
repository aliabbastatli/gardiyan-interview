package com.gardiyan.oms.dto.request.order;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class OrderCreateRequest {
    private UUID customerId;
    private List<OrderItemRequest> items;
} 