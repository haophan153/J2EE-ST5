package com.example.EcoSwap.service;

import com.example.EcoSwap.dto.ProductFilterDTO;
import com.example.EcoSwap.entity.Product;
import com.example.EcoSwap.repository.ProductRepository;
import com.example.EcoSwap.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAvailableProducts(Pageable pageable) {
        return productRepository.findByStatus("AVAILABLE", pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Page<Product> filterProducts(ProductFilterDTO filter, Pageable pageable) {
        if (filter == null || isEmptyFilter(filter)) {
            return productRepository.findByStatus("AVAILABLE", pageable);
        }
        return productRepository.findAll(ProductSpecification.withFilters(filter), pageable);
    }

    public Sort getSortFromFilter(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return Sort.by("createdAt").descending();
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "title" -> Sort.by("title").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }

    private boolean isEmptyFilter(ProductFilterDTO filter) {
        return (filter.getKeyword() == null || filter.getKeyword().isBlank())
                && filter.getCategoryId() == null
                && (filter.getCondition() == null || filter.getCondition().isBlank())
                && filter.getMinPrice() == null
                && filter.getMaxPrice() == null
                && (filter.getLocation() == null || filter.getLocation().isBlank());
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByUser(Long userId) {
        return productRepository.findByUserId(userId);
    }

    public Page<Product> getProductsByUserPaged(Long userId, Pageable pageable) {
        return productRepository.findByUserId(userId, pageable);
    }

    public Page<Product> getUserProductsHistory(Long userId, List<String> statuses, Pageable pageable) {
        return productRepository.findByUserIdAndStatusIn(userId, statuses, pageable);
    }

    public List<Product> getRecommendedProducts(Long productId, int limit) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return List.of();
        Product product = productOpt.get();
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return productRepository.findSimilarByCategory(product.getCategory().getId(), productId, pageable).getContent();
    }

    public List<Product> getRecommendedForUser(Long userId, int limit) {
        List<Product> myProducts = productRepository.findByUserId(userId);
        if (myProducts.isEmpty()) {
            return getAvailableProducts(PageRequest.of(0, limit, Sort.by("createdAt").descending())).getContent();
        }
        Long preferredCategoryId = myProducts.get(0).getCategory().getId();
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return productRepository.findRecommendedByCategoryExcludingUser(preferredCategoryId, userId, pageable).getContent();
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
