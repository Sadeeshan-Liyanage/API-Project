package com.example.api_project.repository;

import com.example.api_project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Product> findBySubCategoryId(Long subCategoryId);
    List<Product> findBySupplierId(Long supplierId);
}
