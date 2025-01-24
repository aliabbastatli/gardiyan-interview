package com.gardiyan.oms.service;

import com.gardiyan.oms.dto.request.customer.CustomerCreateRequest;
import com.gardiyan.oms.dto.request.customer.CustomerUpdateRequest;
import com.gardiyan.oms.dto.response.customer.CustomerDTO;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerCreateRequest request);
    CustomerDTO getCustomerById(UUID id);
    List<CustomerDTO> getAllCustomers();
    CustomerDTO updateCustomer(UUID id, CustomerUpdateRequest request);
    void deleteCustomer(UUID id);
    List<CustomerDTO> searchCustomers(String name, String email, String phone);
    CustomerDTO getCustomerByEmail(String email);
} 