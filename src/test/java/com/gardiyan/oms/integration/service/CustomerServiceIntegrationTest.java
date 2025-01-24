package com.gardiyan.oms.integration.service;

import com.gardiyan.oms.dto.request.customer.CustomerCreateRequest;
import com.gardiyan.oms.dto.request.customer.CustomerUpdateRequest;
import com.gardiyan.oms.dto.response.customer.CustomerDTO;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import com.gardiyan.oms.exception.EmailAlreadyExistsException;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private CustomerCreateRequest createRequest;
    private CustomerUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        
        customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("+90 555 123 4567");
        customer = customerRepository.save(customer);

        createRequest = new CustomerCreateRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Doe");
        createRequest.setEmail("jane.doe@example.com");
        createRequest.setPhone("+90 555 987 6543");

        updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setLastName("Doe Updated");
        updateRequest.setEmail("john.updated@example.com");
        updateRequest.setPhone("+90 555 111 2222");
    }

    @Test
    void createCustomer_Success() {
        // When
        CustomerDTO result = customerService.createCustomer(createRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(createRequest.getEmail(), result.getEmail());
    }

    @Test
    void createCustomer_EmailExists_ThrowsException() {
        // Given
        createRequest.setEmail(customer.getEmail());

        // When & Then
        assertThrows(EmailAlreadyExistsException.class,
            () -> customerService.createCustomer(createRequest));
    }

    @Test
    void getCustomerById_Success() {
        // When
        CustomerDTO result = customerService.getCustomerById(customer.getId());

        // Then
        assertNotNull(result);
        assertEquals(customer.getId(), result.getId());
        assertEquals(customer.getEmail(), result.getEmail());
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> customerService.getCustomerById(UUID.randomUUID()));
    }

    @Test
    void getAllCustomers_Success() {
        // When
        List<CustomerDTO> result = customerService.getAllCustomers();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getId());
    }

    @Test
    void updateCustomer_Success() {
        // When
        CustomerDTO result = customerService.updateCustomer(customer.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(customer.getId(), result.getId());
        assertEquals(updateRequest.getFirstName(), result.getFirstName());
        assertEquals(updateRequest.getEmail(), result.getEmail());
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> customerService.updateCustomer(UUID.randomUUID(), updateRequest));
    }

    @Test
    void deleteCustomer_Success() {
        // When
        customerService.deleteCustomer(customer.getId());

        // Then
        assertFalse(customerRepository.existsById(customer.getId()));
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> customerService.deleteCustomer(UUID.randomUUID()));
    }

    @Test
    void searchCustomers_Success() {
        // When
        List<CustomerDTO> result = customerService.searchCustomers(
            customer.getFirstName(),
            customer.getEmail(),
            customer.getPhone()
        );

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getId());
    }
} 