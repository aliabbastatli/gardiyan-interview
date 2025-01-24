package com.gardiyan.oms.unit.service;

import com.gardiyan.oms.dto.request.order.OrderCreateRequest;
import com.gardiyan.oms.dto.request.order.OrderItemRequest;
import com.gardiyan.oms.dto.request.order.OrderSearchRequest;
import com.gardiyan.oms.dto.response.order.OrderDTO;
import com.gardiyan.oms.dto.response.order.OrderItemDTO;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import com.gardiyan.oms.exception.InsufficientStockException;
import com.gardiyan.oms.exception.OrderNotFoundException;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.model.Order;
import com.gardiyan.oms.model.OrderItem;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.repository.OrderRepository;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderDTO orderDTO;
    private UUID orderId;
    private UUID customerId;
    private UUID productId;
    private OrderCreateRequest createRequest;
    private OrderItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");

        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStockQuantity(10);

        itemRequest = new OrderItemRequest();
        itemRequest.setProductId(productId);
        itemRequest.setQuantity(2);

        createRequest = new OrderCreateRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setItems(List.of(itemRequest));

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(product.getPrice());
        orderItem.calculateTotalPrice();

        order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setOrderItems(List.of(orderItem));
        order.setTotalAmount(orderItem.getTotalPrice());
        order.setCreatedAt(LocalDateTime.now());

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
    }

    @Test
    void createOrder_Success() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdWithLock(productId)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderDTO result = orderService.createOrder(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getItems().size());
        verify(customerRepository).findById(customerId);
        verify(productRepository).findByIdWithLock(productId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_CustomerNotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> orderService.createOrder(createRequest));
        verify(customerRepository).findById(customerId);
        verify(productRepository, never()).findByIdWithLock(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_ProductNotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdWithLock(productId)).thenReturn(null);

        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> orderService.createOrder(createRequest));
        verify(customerRepository).findById(customerId);
        verify(productRepository).findByIdWithLock(productId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdWithLock(productId)).thenReturn(product);
        itemRequest.setQuantity(15); // More than available stock

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> orderService.createOrder(createRequest));
        verify(customerRepository).findById(customerId);
        verify(productRepository).findByIdWithLock(productId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        OrderDTO result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
            () -> orderService.getOrderById(orderId));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getAllOrders_Success() {
        // Given
        when(orderRepository.findAll()).thenReturn(List.of(order));

        // When
        List<OrderDTO> result = orderService.getAllOrders();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrdersByCustomerId_Success() {
        // Given
        when(orderRepository.findByCustomerId(customerId)).thenReturn(List.of(order));

        // When
        List<OrderDTO> result = orderService.getOrdersByCustomerId(customerId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        assertEquals(customerId, result.get(0).getCustomerId());
        verify(orderRepository).findByCustomerId(customerId);
    }

    @Test
    void searchOrders_ByCustomerName_Success() {
        // Given
        OrderSearchRequest searchRequest = new OrderSearchRequest();
        searchRequest.setCustomerName("John");
        when(orderRepository.findByCustomerName("John")).thenReturn(List.of(order));

        // When
        List<OrderDTO> result = orderService.searchOrders(searchRequest);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        verify(orderRepository).findByCustomerName("John");
    }

    @Test
    void searchOrders_ByDateRange_Success() {
        // Given
        OrderSearchRequest searchRequest = new OrderSearchRequest();
        searchRequest.setCustomerId(customerId);
        searchRequest.setStartDate(LocalDateTime.now().minusDays(1));
        searchRequest.setEndDate(LocalDateTime.now().plusDays(1));

        when(orderRepository.searchOrders(
            eq(customerId),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).thenReturn(List.of(order));

        // When
        List<OrderDTO> result = orderService.searchOrders(searchRequest);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        verify(orderRepository).searchOrders(
            eq(customerId),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        );
    }

    @Test
    void deleteOrder_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).deleteById(orderId);

        // When
        orderService.deleteOrder(orderId);

        // Then
        verify(orderRepository).findById(orderId);
        verify(orderRepository).deleteById(orderId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteOrder_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
            () -> orderService.deleteOrder(orderId));
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).deleteById(any());
        verify(productRepository, never()).save(any());
    }
} 