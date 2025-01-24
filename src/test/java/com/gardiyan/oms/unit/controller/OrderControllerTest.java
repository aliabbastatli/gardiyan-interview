package com.gardiyan.oms.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gardiyan.oms.controller.OrderController;
import com.gardiyan.oms.dto.request.order.OrderCreateRequest;
import com.gardiyan.oms.dto.request.order.OrderItemRequest;
import com.gardiyan.oms.dto.request.order.OrderSearchRequest;
import com.gardiyan.oms.dto.response.order.OrderDTO;
import com.gardiyan.oms.dto.response.order.OrderItemDTO;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import com.gardiyan.oms.exception.InsufficientStockException;
import com.gardiyan.oms.exception.OrderNotFoundException;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private UUID orderId;
    private UUID customerId;
    private UUID productId;
    private OrderDTO orderDTO;
    private OrderCreateRequest createRequest;
    private OrderItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(UUID.randomUUID());
        orderItemDTO.setProductId(productId);
        orderItemDTO.setQuantity(2);
        orderItemDTO.setPrice(BigDecimal.valueOf(100));
        orderItemDTO.setTotalPrice(BigDecimal.valueOf(200));

        orderDTO = new OrderDTO();
        orderDTO.setId(orderId);
        orderDTO.setCustomerId(customerId);
        orderDTO.setItems(List.of(orderItemDTO));
        orderDTO.setTotalAmount(BigDecimal.valueOf(200));
        orderDTO.setCreatedAt(LocalDateTime.now());

        itemRequest = new OrderItemRequest();
        itemRequest.setProductId(productId);
        itemRequest.setQuantity(2);

        createRequest = new OrderCreateRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setItems(List.of(itemRequest));
    }

    @Test
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(orderId.toString()))
            .andExpect(jsonPath("$.customerId").value(customerId.toString()))
            .andExpect(jsonPath("$.totalAmount").value(orderDTO.getTotalAmount().doubleValue()));

        verify(orderService).createOrder(any(OrderCreateRequest.class));
    }

    @Test
    void createOrder_CustomerNotFound() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class)))
            .thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(orderService).createOrder(any(OrderCreateRequest.class));
    }

    @Test
    void createOrder_ProductNotFound() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class)))
            .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Product not found"));

        verify(orderService).createOrder(any(OrderCreateRequest.class));
    }

    @Test
    void createOrder_InsufficientStock() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class)))
            .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Insufficient stock"));

        verify(orderService).createOrder(any(OrderCreateRequest.class));
    }

    @Test
    void getOrderById_Success() throws Exception {
        when(orderService.getOrderById(any(UUID.class))).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId.toString()))
            .andExpect(jsonPath("$.customerId").value(customerId.toString()));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    void getOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(orderId))
            .thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Order not found"));

        verify(orderService).getOrderById(orderId);
    }

    @Test
    void getAllOrders_Success() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(orderId.toString()))
            .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrdersByCustomerId_Success() throws Exception {
        when(orderService.getOrdersByCustomerId(any(UUID.class))).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/api/orders/customer/{customerId}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(orderId.toString()))
            .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));

        verify(orderService).getOrdersByCustomerId(customerId);
    }

    @Test
    void searchOrders_Success() throws Exception {
        when(orderService.searchOrders(any(OrderSearchRequest.class))).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/api/orders/search")
                .param("customerName", "John")
                .param("customerId", customerId.toString())
                .param("startDate", LocalDateTime.now().minusDays(1).toString())
                .param("endDate", LocalDateTime.now().plusDays(1).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(orderId.toString()))
            .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));

        verify(orderService).searchOrders(any(OrderSearchRequest.class));
    }

    @Test
    void deleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder(orderId);

        mockMvc.perform(delete("/api/orders/{id}", orderId))
            .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(orderId);
    }

    @Test
    void deleteOrder_NotFound() throws Exception {
        doThrow(new OrderNotFoundException("Order not found"))
            .when(orderService).deleteOrder(orderId);

        mockMvc.perform(delete("/api/orders/{id}", orderId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Order not found"));

        verify(orderService).deleteOrder(orderId);
    }
} 