package com.gardiyan.oms.service.impl;

import com.gardiyan.oms.dto.OrderDTO;
import com.gardiyan.oms.dto.OrderItemDTO;
import com.gardiyan.oms.dto.OrderSearchDTO;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.model.Order;
import com.gardiyan.oms.model.OrderItem;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.repository.OrderRepository;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
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
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
            .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.ZERO);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            Product product = productRepository.findByIdWithLock(itemDTO.getProductId());
            if (product == null) {
                throw new EntityNotFoundException("Product not found: " + itemDTO.getProductId());
            }

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), itemDTO.getQuantity())
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.calculateTotalPrice();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
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
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));
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
        return orderRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> searchOrders(OrderSearchDTO searchDTO) {
        if (searchDTO.getCustomerName() != null && !searchDTO.getCustomerName().isEmpty()) {
            return orderRepository.findByCustomerName(searchDTO.getCustomerName()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        }

        return orderRepository.searchOrders(
            searchDTO.getCustomerId(),
            searchDTO.getStartDate(),
            searchDTO.getEndDate()
        ).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));

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
        dto.setOrderItems(itemDTOs);
        
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