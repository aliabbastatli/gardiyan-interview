package com.gardiyan.oms.integration.service;

import com.gardiyan.oms.dto.request.product.ProductCreateRequest;
import com.gardiyan.oms.dto.request.product.ProductUpdateRequest;
import com.gardiyan.oms.dto.response.product.ProductDTO;
import com.gardiyan.oms.exception.InsufficientStockException;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product product;
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
    void createProduct_Success() {
        // When
        ProductDTO result = productService.createProduct(createRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(createRequest.getName(), result.getName());
        assertEquals(createRequest.getPrice(), result.getPrice());
    }

    @Test
    void getProductById_Success() {
        // When
        ProductDTO result = productService.getProductById(product.getId());

        // Then
        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.getProductById(UUID.randomUUID()));
    }

    @Test
    void getAllProducts_Success() {
        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(product.getId(), result.get(0).getId());
    }

    @Test
    void updateProduct_Success() {
        // When
        ProductDTO result = productService.updateProduct(product.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(updateRequest.getName(), result.getName());
        assertEquals(updateRequest.getPrice(), result.getPrice());
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.updateProduct(UUID.randomUUID(), updateRequest));
    }

    @Test
    void deleteProduct_Success() {
        // When
        productService.deleteProduct(product.getId());

        // Then
        assertFalse(productRepository.existsById(product.getId()));
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.deleteProduct(UUID.randomUUID()));
    }

    @Test
    void updateStock_Success() {
        // When
        ProductDTO result = productService.updateStock(product.getId(), 5);

        // Then
        assertNotNull(result);
        assertEquals(15, result.getStockQuantity());
    }

    @Test
    void updateStock_InsufficientStock_ThrowsException() {
        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> productService.updateStock(product.getId(), -15));
    }

    @Test
    void getProductsInStock_Success() {
        // When
        List<ProductDTO> result = productService.getProductsInStock();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void searchProducts_Success() {
        // When
        List<ProductDTO> result = productService.searchProducts(
            product.getName(),
            BigDecimal.valueOf(50),
            BigDecimal.valueOf(150),
            5
        );

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(product.getId(), result.get(0).getId());
    }
} 