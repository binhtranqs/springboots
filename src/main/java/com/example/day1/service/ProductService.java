package com.example.day1.service;

import com.example.day1.dto.CreateProductRequest;
import com.example.day1.dto.PageResponse;
import com.example.day1.dto.ProductResponse;
import com.example.day1.dto.UpdateProductRequest;
import com.example.day1.exception.ResourceNotFoundException;
import com.example.day1.model.Product;
import com.example.day1.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "price");

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResponse<ProductResponse> getProducts(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String keyword
    ) {
        validatePagination(page, size);
        validateSortBy(sortBy);

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;

        if (keyword == null || keyword.isBlank()) {
            productPage = productRepository.findAll(pageable);
        } else {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return toResponse(product);
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProductOrThrow(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
    }

    private void validateSortBy(String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
    }

    private Sort createSort(String sortBy, String sortDir) {
        if ("desc".equalsIgnoreCase(sortDir)) {
            return Sort.by(sortBy).descending();
        }

        if ("asc".equalsIgnoreCase(sortDir)) {
            return Sort.by(sortBy).ascending();
        }

        throw new IllegalArgumentException("Invalid sort direction: " + sortDir);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        );
    }
}
