package com.gardiyan.oms.service;

import com.gardiyan.oms.dto.request.product.ProductCreateRequest;
import com.gardiyan.oms.dto.request.product.ProductUpdateRequest;
import com.gardiyan.oms.dto.response.product.ProductDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDTO createProduct(ProductCreateRequest request);
    ProductDTO getProductById(UUID id);
    List<ProductDTO> getAllProducts();
    ProductDTO updateProduct(UUID id, ProductUpdateRequest request);
    void deleteProduct(UUID id);
    ProductDTO updateStock(UUID id, int quantity);
    List<ProductDTO> getProductsInStock();
    List<ProductDTO> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minStock);
} 