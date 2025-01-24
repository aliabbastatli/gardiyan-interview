package com.gardiyan.oms.repository.spec;

import com.gardiyan.oms.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {
    
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
    
    public static Specification<Product> descriptionContains(String description) {
        return (root, query, cb) -> {
            if (description == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }
    
    public static Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            }
            if (minPrice == null) {
                return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
            if (maxPrice == null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }
    
    public static Specification<Product> stockGreaterThanOrEqual(Integer minStock) {
        return (root, query, cb) -> {
            if (minStock == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("stockQuantity"), minStock);
        };
    }
    
    public static Specification<Product> hasStockLessThan(Integer maxStock) {
        return (root, query, cb) -> {
            if (maxStock == null) {
                return null;
            }
            return cb.lessThan(root.get("stockQuantity"), maxStock);
        };
    }
} 