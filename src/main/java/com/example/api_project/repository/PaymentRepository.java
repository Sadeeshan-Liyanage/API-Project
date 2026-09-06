package com.example.api_project.repository;

import com.example.api_project.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySalesOrderId(Long salesOrderId);
}
