package com.example.api_project.repository;

import com.grocery.api.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByBranchId(Long branchId);
    List<SalesOrder> findByCustomerId(Long customerId);
    List<SalesOrder> findByCashierId(Long cashierId);
    List<SalesOrder> findByStatus(SalesOrder.Status status);
}
