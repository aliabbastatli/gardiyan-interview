package com.gardiyan.oms.service;

import com.gardiyan.oms.dto.request.order.OrderCreateRequest;
import com.gardiyan.oms.dto.request.order.OrderSearchRequest;
import com.gardiyan.oms.dto.response.order.OrderDTO;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDTO createOrder(OrderCreateRequest request);
    OrderDTO getOrderById(UUID id);
    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByCustomerId(UUID customerId);
    void deleteOrder(UUID id);
    List<OrderDTO> searchOrders(OrderSearchRequest request);
} 