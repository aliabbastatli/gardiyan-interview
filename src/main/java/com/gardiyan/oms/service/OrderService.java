package com.gardiyan.oms.service;

import com.gardiyan.oms.dto.OrderDTO;
import com.gardiyan.oms.dto.OrderSearchDTO;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO getOrderById(UUID id);
    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByCustomerId(UUID customerId);
    List<OrderDTO> searchOrders(OrderSearchDTO searchDTO);
    void deleteOrder(UUID id);
} 