package com.example.EcoSwap.repository;

import com.example.EcoSwap.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByStatus(String status, Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    List<Product> findByUserId(Long userId);
    Page<Product> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.user.id = :userId AND p.status IN :statuses")
    Page<Product> findByUserIdAndStatusIn(@Param("userId") Long userId,
                                          @Param("statuses") List<String> statuses,
                                          Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'AVAILABLE' AND p.category.id = :categoryId AND p.id != :excludeId")
    Page<Product> findSimilarByCategory(Long categoryId, Long excludeId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'AVAILABLE' AND p.category.id = :categoryId AND p.user.id != :excludeUserId")
    Page<Product> findRecommendedByCategoryExcludingUser(Long categoryId, Long excludeUserId, Pageable pageable);
}
