package com.gardiyan.oms.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gardiyan.oms.controller.ProductController;
import com.gardiyan.oms.dto.request.product.ProductCreateRequest;
import com.gardiyan.oms.dto.request.product.ProductUpdateRequest;
import com.gardiyan.oms.dto.response.product.ProductDTO;
import com.gardiyan.oms.exception.InsufficientStockException;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private UUID productId;
    private ProductDTO productDTO;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        productDTO = new ProductDTO();
        productDTO.setId(productId);
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(BigDecimal.valueOf(100));
        productDTO.setStockQuantity(10);

        createRequest = new ProductCreateRequest();
        createRequest.setName("Test Product");
        createRequest.setDescription("Test Description");
        createRequest.setPrice(BigDecimal.valueOf(100));
        createRequest.setStockQuantity(10);

        updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(BigDecimal.valueOf(150));
        updateRequest.setStockQuantity(15);
    }

    @Test
    void createProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(productDTO);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value(productDTO.getName()))
            .andExpect(jsonPath("$.price").value(productDTO.getPrice().doubleValue()));

        verify(productService).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    void getProductById_Success() throws Exception {
        when(productService.getProductById(any(UUID.class))).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value(productDTO.getName()))
            .andExpect(jsonPath("$.price").value(productDTO.getPrice().doubleValue()));

        verify(productService).getProductById(productId);
    }

    @Test
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(productId))
            .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService).getProductById(productId);
    }

    @Test
    void getAllProducts_Success() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(productId.toString()))
            .andExpect(jsonPath("$[0].name").value(productDTO.getName()));

        verify(productService).getAllProducts();
    }

    @Test
    void updateProduct_Success() throws Exception {
        when(productService.updateProduct(any(UUID.class), any(ProductUpdateRequest.class)))
            .thenReturn(productDTO);

        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value(productDTO.getName()));

        verify(productService).updateProduct(eq(productId), any(ProductUpdateRequest.class));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/products/{id}", productId))
            .andExpect(status().isNoContent());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void updateStock_Success() throws Exception {
        when(productService.updateStock(any(UUID.class), anyInt())).thenReturn(productDTO);

        mockMvc.perform(patch("/api/products/{id}/stock", productId)
                .param("quantity", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.stockQuantity").value(productDTO.getStockQuantity()));

        verify(productService).updateStock(eq(productId), eq(5));
    }

    @Test
    void updateStock_InsufficientStock() throws Exception {
        when(productService.updateStock(any(UUID.class), anyInt()))
            .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(patch("/api/products/{id}/stock", productId)
                .param("quantity", "-15"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Insufficient stock"));

        verify(productService).updateStock(eq(productId), eq(-15));
    }

    @Test
    void getProductsInStock_Success() throws Exception {
        when(productService.getProductsInStock()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/api/products/in-stock"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(productId.toString()));

        verify(productService).getProductsInStock();
    }

    @Test
    void searchProducts_Success() throws Exception {
        when(productService.searchProducts(anyString(), any(), any(), anyInt()))
            .thenReturn(List.of(productDTO));

        mockMvc.perform(get("/api/products/search")
                .param("name", "Test")
                .param("minPrice", "50")
                .param("maxPrice", "150")
                .param("minStock", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(productId.toString()))
            .andExpect(jsonPath("$[0].name").value(productDTO.getName()));

        verify(productService).searchProducts(eq("Test"), 
            eq(BigDecimal.valueOf(50)), 
            eq(BigDecimal.valueOf(150)), 
            eq(5));
    }
} 