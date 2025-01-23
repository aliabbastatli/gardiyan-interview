package com.gardiyan.oms.service;

import com.gardiyan.oms.dto.ProductDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(UUID id, ProductDTO productDTO);
    void deleteProduct(UUID id);
    ProductDTO getProductById(UUID id);
    List<ProductDTO> getAllProducts();
    void updateStock(UUID id, int quantity);
    List<ProductDTO> getProductsInStock();
} 