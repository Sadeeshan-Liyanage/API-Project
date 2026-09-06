package com.example.api_project.repository;

import com.example.api_project.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
