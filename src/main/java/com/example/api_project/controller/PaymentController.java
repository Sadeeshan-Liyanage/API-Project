package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.Payment;
import com.example.api_project.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getAll(
            @RequestParam(required = false) Long salesOrderId) {
        List<Payment> result = salesOrderId != null
                ? paymentService.findBySalesOrder(salesOrderId)
                : paymentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.findById(id)));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> create(@Valid @RequestBody Payment payment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", paymentService.create(payment)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted", null));
    }
}

