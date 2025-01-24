package com.gardiyan.oms.unit.service;

import com.gardiyan.oms.dto.request.product.ProductCreateRequest;
import com.gardiyan.oms.dto.request.product.ProductUpdateRequest;
import com.gardiyan.oms.dto.response.product.ProductDTO;
import com.gardiyan.oms.exception.InsufficientStockException;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;
    private UUID productId;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStockQuantity(10);

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
    void createProduct_Success() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductDTO result = productService.createProduct(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(createRequest.getName(), result.getName());
        assertEquals(createRequest.getPrice(), result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductDTO result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.getProductById(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    void getAllProducts_Success() {
        // Given
        List<Product> products = List.of(product);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_Success() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductDTO result = productService.updateProduct(productId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getName(), result.getName());
        assertEquals(updateRequest.getPrice(), result.getPrice());
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.updateProduct(productId, updateRequest));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        // Given
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        // Given
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.deleteProduct(productId));
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void updateStock_Success() {
        // Given
        when(productRepository.findByIdWithLock(productId)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductDTO result = productService.updateStock(productId, 5);

        // Then
        assertNotNull(result);
        assertEquals(15, result.getStockQuantity());
        verify(productRepository).findByIdWithLock(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateStock_InsufficientStock_ThrowsException() {
        // Given
        when(productRepository.findByIdWithLock(productId)).thenReturn(product);

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> productService.updateStock(productId, -15));
        verify(productRepository).findByIdWithLock(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductsInStock_Success() {
        // Given
        List<Product> products = List.of(product);
        when(productRepository.findByStockQuantityGreaterThan(0)).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getProductsInStock();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(productRepository).findByStockQuantityGreaterThan(0);
    }

    @Test
    void searchProducts_Success() {
        // Given
        List<Product> products = List.of(product);
        when(productRepository.findAll(any(Specification.class))).thenReturn(products);

        // When
        List<ProductDTO> result = productService.searchProducts(
            "Test",
            BigDecimal.valueOf(50),
            BigDecimal.valueOf(200),
            5
        );

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(productRepository).findAll(any(Specification.class));
    }
} 