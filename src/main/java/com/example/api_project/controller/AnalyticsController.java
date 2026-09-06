package com.example.api_project.controller;

import com.example.api_project.dto.ApiResponse;
import com.example.api_project.dto.analytics.*;
import com.example.api_project.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummary>> summary() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSummary()));
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<ApiResponse<List<RevenuePoint>>> revenueTrend(
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRevenueTrend(days)));
    }



    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProduct>>> topProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTopProducts(limit)));
    }

    @GetMapping("/top-customers")
    public ResponseEntity<ApiResponse<List<TopCustomer>>> topCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTopCustomers(limit)));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodShare>>> paymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPaymentBreakdown()));
    }
}









