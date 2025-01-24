package com.gardiyan.oms.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gardiyan.oms.dto.request.product.ProductCreateRequest;
import com.gardiyan.oms.dto.request.product.ProductUpdateRequest;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product product;
    private UUID productId;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStockQuantity(10);
        product = productRepository.save(product);
        productId = product.getId();

        createRequest = new ProductCreateRequest();
        createRequest.setName("New Product");
        createRequest.setDescription("New Description");
        createRequest.setPrice(BigDecimal.valueOf(150));
        createRequest.setStockQuantity(15);

        updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(BigDecimal.valueOf(200));
        updateRequest.setStockQuantity(20);
    }

    @Test
    void createProduct_Success() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(createRequest.getName()))
            .andExpect(jsonPath("$.price").value(createRequest.getPrice().doubleValue()));
    }

    @Test
    void getProductById_Success() throws Exception {
        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value(product.getName()))
            .andExpect(jsonPath("$.price").value(product.getPrice().doubleValue()));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(productId.toString()))
            .andExpect(jsonPath("$[0].name").value(product.getName()));
    }

    @Test
    void updateProduct_Success() throws Exception {
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value(updateRequest.getName()))
            .andExpect(jsonPath("$.price").value(updateRequest.getPrice().doubleValue()));
    }

    @Test
    void updateProduct_NotFound() throws Exception {
        mockMvc.perform(put("/api/products/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", productId))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateStock_Success() throws Exception {
        mockMvc.perform(patch("/api/products/{id}/stock", productId)
                .param("quantity", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.stockQuantity").value(15));
    }

    @Test
    void updateStock_InsufficientStock() throws Exception {
        mockMvc.perform(patch("/api/products/{id}/stock", productId)
                .param("quantity", "-15"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsInStock_Success() throws Exception {
        mockMvc.perform(get("/api/products/in-stock"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(productId.toString()));
    }

    @Test
    void searchProducts_Success() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("name", product.getName())
                .param("minPrice", "50")
                .param("maxPrice", "150")
                .param("minStock", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(productId.toString()))
            .andExpect(jsonPath("$[0].name").value(product.getName()));
    }
} 