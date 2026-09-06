package com.example.api_project.repository;

import com.example.api_project.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByBranchId(Long branchId);
    List<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByProductIdAndBranchId(Long productId, Long branchId);
    List<Inventory> findByQuantityLessThan(Integer threshold);
}
