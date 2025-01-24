package com.gardiyan.oms.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gardiyan.oms.controller.CustomerController;
import com.gardiyan.oms.dto.request.customer.CustomerCreateRequest;
import com.gardiyan.oms.dto.request.customer.CustomerUpdateRequest;
import com.gardiyan.oms.dto.response.customer.CustomerDTO;
import com.gardiyan.oms.exception.CustomerNotFoundException;
import com.gardiyan.oms.exception.EmailAlreadyExistsException;
import com.gardiyan.oms.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private UUID customerId;
    private CustomerDTO customerDTO;
    private CustomerCreateRequest createRequest;
    private CustomerUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

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
        updateRequest.setPhone("+90 555 987 6543");
    }

    @Test
    void createCustomer_Success() throws Exception {
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(customerDTO);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.firstName").value(customerDTO.getFirstName()))
            .andExpect(jsonPath("$.email").value(customerDTO.getEmail()));

        verify(customerService).createCustomer(any(CustomerCreateRequest.class));
    }

    @Test
    void createCustomer_EmailExists() throws Exception {
        when(customerService.createCustomer(any(CustomerCreateRequest.class)))
            .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Email already exists"));

        verify(customerService).createCustomer(any(CustomerCreateRequest.class));
    }

    @Test
    void getCustomerById_Success() throws Exception {
        when(customerService.getCustomerById(any(UUID.class))).thenReturn(customerDTO);

        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.firstName").value(customerDTO.getFirstName()))
            .andExpect(jsonPath("$.email").value(customerDTO.getEmail()));

        verify(customerService).getCustomerById(customerId);
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        when(customerService.getCustomerById(customerId))
            .thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(customerService).getCustomerById(customerId);
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(customerId.toString()))
            .andExpect(jsonPath("$[0].firstName").value(customerDTO.getFirstName()));

        verify(customerService).getAllCustomers();
    }

    @Test
    void updateCustomer_Success() throws Exception {
        when(customerService.updateCustomer(any(UUID.class), any(CustomerUpdateRequest.class)))
            .thenReturn(customerDTO);

        mockMvc.perform(put("/api/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.firstName").value(customerDTO.getFirstName()));

        verify(customerService).updateCustomer(eq(customerId), any(CustomerUpdateRequest.class));
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        doNothing().when(customerService).deleteCustomer(customerId);

        mockMvc.perform(delete("/api/customers/{id}", customerId))
            .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(customerId);
    }

    @Test
    void searchCustomers_Success() throws Exception {
        when(customerService.searchCustomers(anyString(), anyString(), anyString()))
            .thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/api/customers/search")
                .param("name", "John")
                .param("email", "john.doe")
                .param("phone", "1234"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(customerId.toString()))
            .andExpect(jsonPath("$[0].firstName").value(customerDTO.getFirstName()));

        verify(customerService).searchCustomers(eq("John"), eq("john.doe"), eq("1234"));
    }
} 