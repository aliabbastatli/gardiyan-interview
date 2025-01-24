package com.gardiyan.oms.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gardiyan.oms.dto.request.customer.CustomerCreateRequest;
import com.gardiyan.oms.dto.request.customer.CustomerUpdateRequest;
import com.gardiyan.oms.model.Customer;
import com.gardiyan.oms.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private UUID customerId;
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
        customerId = customer.getId();

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
    void createCustomer_Success() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(createRequest.getEmail()));
    }

    @Test
    void createCustomer_EmailExists() throws Exception {
        createRequest.setEmail(customer.getEmail());

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getCustomerById_Success() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.firstName").value(customer.getFirstName()))
            .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    void getCustomerById_NotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(customerId.toString()))
            .andExpect(jsonPath("$[0].firstName").value(customer.getFirstName()));
    }

    @Test
    void updateCustomer_Success() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.firstName").value(updateRequest.getFirstName()))
            .andExpect(jsonPath("$.email").value(updateRequest.getEmail()));
    }

    @Test
    void updateCustomer_NotFound() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", customerId))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_NotFound() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void searchCustomers_Success() throws Exception {
        mockMvc.perform(get("/api/customers/search")
                .param("name", customer.getFirstName())
                .param("email", customer.getEmail())
                .param("phone", customer.getPhone()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(customerId.toString()))
            .andExpect(jsonPath("$[0].firstName").value(customer.getFirstName()));
    }
} 