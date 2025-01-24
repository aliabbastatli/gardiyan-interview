package com.gardiyan.oms.service.impl;

import com.gardiyan.oms.dto.request.customer.CustomerCreateRequest;
import com.gardiyan.oms.dto.request.customer.CustomerUpdateRequest;
import com.gardiyan.oms.dto.response.customer.CustomerDTO;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.repository.spec.CustomerSpecification;
import com.gardiyan.oms.service.CustomerService;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    public CustomerDTO createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        
        return mapToDTO(customerRepository.save(customer));
    }

    @Override
    public CustomerDTO getCustomerById(UUID id) {
        return customerRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO updateCustomer(UUID id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
            
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        
        return mapToDTO(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    @Override
    public List<CustomerDTO> searchCustomers(String name, String email, String phone) {
        Specification<Customer> spec = Specification.where(null);
        
        if (name != null) {
            spec = spec.and(CustomerSpecification.firstNameContains(name)
                .or(CustomerSpecification.lastNameContains(name)));
        }
        if (email != null) {
            spec = spec.and(CustomerSpecification.hasEmail(email));
        }
        if (phone != null) {
            spec = spec.and(CustomerSpecification.phoneContains(phone));
        }
        
        return customerRepository.findAll(spec).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
            .map(this::mapToDTO)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        return dto;
    }
} 