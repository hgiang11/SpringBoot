package com.thg.chickenshop.repository;

import com.thg.chickenshop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p
            WHERE p.available = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Product> search(@Param("categoryId") Long categoryId,
                         @Param("keyword") String keyword,
                         Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByCategoryId(Long categoryId);

}