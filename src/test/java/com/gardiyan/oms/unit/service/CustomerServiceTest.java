package com.gardiyan.oms.unit.service;

import com.gardiyan.oms.dto.request.customer.CustomerCreateRequest;
import com.gardiyan.oms.dto.request.customer.CustomerUpdateRequest;
import com.gardiyan.oms.dto.response.customer.CustomerDTO;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import com.gardiyan.oms.exception.EmailAlreadyExistsException;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.repository.CustomerRepository;
import com.gardiyan.oms.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerDTO customerDTO;
    private UUID customerId;
    private CustomerCreateRequest createRequest;
    private CustomerUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        
        customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("+90 555 123 4567");

        customerDTO = new CustomerDTO();
        customerDTO.setId(customerId);
        customerDTO.setFirstName("John");
        customerDTO.setLastName("Doe");
        customerDTO.setEmail("john.doe@example.com");
        customerDTO.setPhone("+90 555 123 4567");

        createRequest = new CustomerCreateRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@example.com");
        createRequest.setPhone("+90 555 123 4567");

        updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setLastName("Doe Updated");
        updateRequest.setEmail("john.updated@example.com");
        updateRequest.setPhone("+90 555 123 4568");
    }

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        CustomerDTO result = customerService.createCustomer(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(customerDTO.getEmail(), result.getEmail());
        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_EmailExists_ThrowsException() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class,
            () -> customerService.createCustomer(createRequest));
        verify(customerRepository).existsByEmail(createRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        CustomerDTO result = customerService.getCustomerById(customerId);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getId());
        verify(customerRepository).findById(customerId);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> customerService.getCustomerById(customerId));
        verify(customerRepository).findById(customerId);
    }

    @Test
    void getAllCustomers_Success() {
        // Given
        List<Customer> customers = List.of(customer);
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<CustomerDTO> result = customerService.getAllCustomers();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(customerRepository).findAll();
    }

    @Test
    void updateCustomer_Success() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        CustomerDTO result = customerService.updateCustomer(customerId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getFirstName(), result.getFirstName());
        assertEquals(updateRequest.getLastName(), result.getLastName());
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> customerService.updateCustomer(customerId, updateRequest));
        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        // Given
        when(customerRepository.existsById(customerId)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(customerId);

        // When
        customerService.deleteCustomer(customerId);

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // Given
        when(customerRepository.existsById(customerId)).thenReturn(false);

        // When & Then
        assertThrows(CustomerNotFoundException.class,
            () -> customerService.deleteCustomer(customerId));
        verify(customerRepository).existsById(customerId);
        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void searchCustomers_Success() {
        // Given
        List<Customer> customers = List.of(customer);
        when(customerRepository.findAll(any(Specification.class))).thenReturn(customers);

        // When
        List<CustomerDTO> result = customerService.searchCustomers("John", null, null);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(customerRepository).findAll(any(Specification.class));
    }
} 