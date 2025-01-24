package com.gardiyan.oms.service.impl;

import com.gardiyan.oms.dto.request.product.ProductCreateRequest;
import com.gardiyan.oms.dto.request.product.ProductUpdateRequest;
import com.gardiyan.oms.dto.response.product.ProductDTO;
import com.gardiyan.oms.exception.ProductNotFoundException;
import com.gardiyan.oms.model.Product;
import com.gardiyan.oms.repository.ProductRepository;
import com.gardiyan.oms.repository.spec.ProductSpecification;
import com.gardiyan.oms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public ProductDTO createProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        
        return mapToDTO(productRepository.save(product));
    }

    @Override
    public ProductDTO getProductById(UUID id) {
        return productRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        return mapToDTO(productRepository.save(product));
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductDTO updateStock(UUID id, int quantity) {
        Product product = productRepository.findByIdWithLock(id);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        product.setStockQuantity(product.getStockQuantity() + quantity);
        return mapToDTO(productRepository.save(product));
    }

    @Override
    public List<ProductDTO> getProductsInStock() {
        return productRepository.findByStockQuantityGreaterThan(0).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Integer minStock) {
        Specification<Product> spec = Specification.where(null);

        if (name != null) {
            spec = spec.and(ProductSpecification.nameContains(name));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.priceBetween(minPrice, maxPrice));
        }
        if (minStock != null) {
            spec = spec.and(ProductSpecification.stockGreaterThanOrEqual(minStock));
        }

        return productRepository.findAll(spec).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
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