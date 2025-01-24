package com.gardiyan.oms.integration.service;

import com.gardiyan.oms.dto.request.order.OrderCreateRequest;
import com.gardiyan.oms.dto.request.order.OrderItemRequest;
import com.gardiyan.oms.dto.request.order.OrderSearchRequest;
import com.gardiyan.oms.dto.response.order.OrderDTO;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import com.gardiyan.oms.exception.InsufficientStockException;
import com.gardiyan.oms.exception.OrderNotFoundException;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.repository.OrderRepository;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

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

        // Prepare create request
        itemRequest = new OrderItemRequest();
        itemRequest.setProductId(product.getId());
        itemRequest.setQuantity(2);

        createRequest = new OrderCreateRequest();
        createRequest.setCustomerId(customer.getId());
        createRequest.setItems(List.of(itemRequest));
    }

    @Test
    void createOrder_Success() {
        // When
        OrderDTO result = orderService.createOrder(createRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(customer.getId(), result.getCustomerId());
        assertEquals(1, result.getItems().size());
        assertEquals(product.getId(), result.getItems().get(0).getProductId());
        assertEquals(2, result.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(200), result.getTotalAmount());

        // Verify product stock is updated
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(8, updatedProduct.getStockQuantity());
    }

    @Test
    void createOrder_CustomerNotFound_ThrowsException() {
        // Given
        createRequest.setCustomerId(UUID.randomUUID());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> orderService.createOrder(createRequest));
    }

    @Test
    void createOrder_ProductNotFound_ThrowsException() {
        // Given
        itemRequest.setProductId(UUID.randomUUID());
        createRequest.setItems(List.of(itemRequest));

        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> orderService.createOrder(createRequest));
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        itemRequest.setQuantity(15); // More than available stock

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> orderService.createOrder(createRequest));
    }

    @Test
    void getOrderById_Success() {
        // Given
        OrderDTO order = orderService.createOrder(createRequest);

        // When
        OrderDTO result = orderService.getOrderById(order.getId());

        // Then
        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(customer.getId(), result.getCustomerId());
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        // When & Then
        assertThrows(OrderNotFoundException.class,
            () -> orderService.getOrderById(UUID.randomUUID()));
    }

    @Test
    void getAllOrders_Success() {
        // Given
        orderService.createOrder(createRequest);

        // When
        List<OrderDTO> result = orderService.getAllOrders();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByCustomerId_Success() {
        // Given
        orderService.createOrder(createRequest);

        // When
        List<OrderDTO> result = orderService.getOrdersByCustomerId(customer.getId());

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getCustomerId());
    }

    @Test
    void searchOrders_ByCustomerName_Success() {
        // Given
        orderService.createOrder(createRequest);
        OrderSearchRequest searchRequest = new OrderSearchRequest();
        searchRequest.setCustomerName(customer.getFirstName());

        // When
        List<OrderDTO> result = orderService.searchOrders(searchRequest);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getCustomerId());
    }

    @Test
    void searchOrders_ByDateRange_Success() {
        // Given
        orderService.createOrder(createRequest);
        OrderSearchRequest searchRequest = new OrderSearchRequest();
        searchRequest.setCustomerId(customer.getId());
        searchRequest.setStartDate(LocalDateTime.now().minusDays(1));
        searchRequest.setEndDate(LocalDateTime.now().plusDays(1));

        // When
        List<OrderDTO> result = orderService.searchOrders(searchRequest);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getCustomerId());
    }

    @Test
    void deleteOrder_Success() {
        // Given
        int initialStock = product.getStockQuantity(); // Get initial stock before creating order
        OrderDTO order = orderService.createOrder(createRequest);

        // When
        orderService.deleteOrder(order.getId());

        // Then
        assertFalse(orderRepository.existsById(order.getId()));
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(initialStock, updatedProduct.getStockQuantity());
    }

    @Test
    void deleteOrder_NotFound_ThrowsException() {
        // When & Then
        assertThrows(OrderNotFoundException.class,
            () -> orderService.deleteOrder(UUID.randomUUID()));
    }
} 