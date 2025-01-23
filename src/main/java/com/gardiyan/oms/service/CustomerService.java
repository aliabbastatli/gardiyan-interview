package com.gardiyan.oms.service;

import com.gardiyan.oms.dto.CustomerDTO;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerDTO customerDTO);
    CustomerDTO updateCustomer(UUID id, CustomerDTO customerDTO);
    void deleteCustomer(UUID id);
    CustomerDTO getCustomerById(UUID id);
    List<CustomerDTO> getAllCustomers();
    CustomerDTO getCustomerByEmail(String email);
} 