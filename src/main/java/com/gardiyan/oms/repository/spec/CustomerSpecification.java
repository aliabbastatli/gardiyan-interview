package com.gardiyan.oms.repository.spec;

import com.gardiyan.oms.model.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {
    
    public static Specification<Customer> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null) {
                return null;
            }
            return cb.equal(root.get("email"), email);
        };
    }
    
    public static Specification<Customer> firstNameContains(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }
    
    public static Specification<Customer> lastNameContains(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }
    
    public static Specification<Customer> phoneContains(String phone) {
        return (root, query, cb) -> {
            if (phone == null) {
                return null;
            }
            return cb.like(root.get("phone"), "%" + phone + "%");
        };
    }
} 