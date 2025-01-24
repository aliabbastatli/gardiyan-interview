package com.gardiyan.oms.repository.spec;

import com.gardiyan.oms.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderSpecification {
    
    public static Specification<Order> hasCustomerId(UUID customerId) {
        return (root, query, cb) -> {
            if (customerId == null) {
                return null;
            }
            return cb.equal(root.get("customer").get("id"), customerId);
        };
    }
    
    public static Specification<Order> customerNameContains(String customerName) {
        return (root, query, cb) -> {
            if (customerName == null) {
                return null;
            }
            String pattern = "%" + customerName.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("customer").get("firstName")), pattern),
                cb.like(cb.lower(root.get("customer").get("lastName")), pattern)
            );
        };
    }
    
    public static Specification<Order> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate == null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
            if (endDate == null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            return cb.between(root.get("createdAt"), startDate, endDate);
        };
    }
    
    public static Specification<Order> totalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (minAmount == null && maxAmount == null) {
                return null;
            }
            if (minAmount == null) {
                return cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount);
            }
            if (maxAmount == null) {
                return cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
            }
            return cb.between(root.get("totalAmount"), minAmount, maxAmount);
        };
    }
} 