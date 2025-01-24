package com.gardiyan.oms.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gardiyan.oms.dto.request.order.OrderCreateRequest;
import com.gardiyan.oms.dto.request.order.OrderItemRequest;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.repository.OrderRepository;
import com.gardiyan.oms.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer customer;
    private Product product;
    private OrderCreateRequest createRequest;
    private OrderItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        
        // Create customer
        customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("+90 555 123 4567");
        customer = customerRepository.save(customer);

        // Create product
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStockQuantity(10);
        product = productRepository.save(product);

        // Create order request
        itemRequest = new OrderItemRequest();
        itemRequest.setProductId(product.getId());
        itemRequest.setQuantity(2);

        createRequest = new OrderCreateRequest();
        createRequest.setCustomerId(customer.getId());
        createRequest.setItems(List.of(itemRequest));
    }

    @Test
    void createOrder_Success() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerId").value(customer.getId().toString()));
    }

    @Test
    void createOrder_CustomerNotFound() throws Exception {
        createRequest.setCustomerId(UUID.randomUUID());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_ProductNotFound() throws Exception {
        itemRequest.setProductId(UUID.randomUUID());
        createRequest.setItems(List.of(itemRequest));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_InsufficientStock() throws Exception {
        itemRequest.setQuantity(15); // More than available stock
        createRequest.setItems(List.of(itemRequest));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_Success() throws Exception {
        // First create an order
        String response = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andReturn().getResponse().getContentAsString();
        
        String orderId = objectMapper.readTree(response).get("id").asText();

        // Then get it by ID
        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.customerId").value(customer.getId().toString()));
    }

    @Test
    void getOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllOrders_Success() throws Exception {
        // First create an order
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated());

        // Then get all orders
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].customerId").value(customer.getId().toString()));
    }

    @Test
    void getOrdersByCustomerId_Success() throws Exception {
        // First create an order
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated());

        // Then get orders by customer ID
        mockMvc.perform(get("/api/orders/customer/{customerId}", customer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].customerId").value(customer.getId().toString()));
    }

    @Test
    void searchOrders_Success() throws Exception {
        // First create an order
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated());

        // Then search orders
        mockMvc.perform(get("/api/orders/search")
                .param("customerName", customer.getFirstName())
                .param("customerId", customer.getId().toString())
                .param("startDate", LocalDateTime.now().minusDays(1).toString())
                .param("endDate", LocalDateTime.now().plusDays(1).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].customerId").value(customer.getId().toString()));
    }

    @Test
    void deleteOrder_Success() throws Exception {
        // First create an order
        String response = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andReturn().getResponse().getContentAsString();
        
        String orderId = objectMapper.readTree(response).get("id").asText();

        // Then delete it
        mockMvc.perform(delete("/api/orders/{id}", orderId))
            .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_NotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
} 