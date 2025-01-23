package com.gardiyan.oms.service.impl;

import com.gardiyan.oms.dto.ProductDTO;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        validateProduct(productDTO);
        Product product = mapToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO) {
        validateProduct(productDTO);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());

        return mapToDTO(productRepository.save(product));
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        return productRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStock(UUID id, int quantity) {
        Product product = productRepository.findByIdWithLock(id);
        if (product == null) {
            throw new EntityNotFoundException("Product not found");
        }

        int newQuantity = product.getStockQuantity() + quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        product.setStockQuantity(newQuantity);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsInStock() {
        return productRepository.findByStockQuantityGreaterThan(0).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    private void validateProduct(ProductDTO productDTO) {
        if (productDTO.getPrice() != null && productDTO.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must be non-negative");
        }
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        return product;
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
} 