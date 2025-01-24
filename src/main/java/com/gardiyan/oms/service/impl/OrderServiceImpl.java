package com.gardiyan.oms.service.impl;

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
import com.gardiyan.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderDTO createOrder(OrderCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.ZERO);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findByIdWithLock(itemRequest.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("Product not found: " + itemRequest.getProductId());
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), itemRequest.getQuantity())
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.calculateTotalPrice();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID id) {
        return orderRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found");
        }
        return orderRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> searchOrders(OrderSearchRequest searchRequest) {
        if (searchRequest.getCustomerName() != null && !searchRequest.getCustomerName().isEmpty()) {
            return orderRepository.findByCustomerName(searchRequest.getCustomerName()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        }

        return orderRepository.searchOrders(
            searchRequest.getCustomerId(),
            searchRequest.getStartDate(),
            searchRequest.getEndDate()
        ).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        // Restore product stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.deleteById(id);
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
            .map(this::mapToOrderItemDTO)
            .collect(Collectors.toList());
        dto.setItems(itemDTOs);
        
        return dto;
    }

    private OrderItemDTO mapToOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
} 