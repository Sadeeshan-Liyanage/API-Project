package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.SalesOrder;
import com.example.api_project.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesOrder>>> getAll(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long customerId) {
        List<SalesOrder> result;
        if (branchId != null) {
            result = salesOrderService.findByBranch(branchId);
        } else if (customerId != null) {
            result = salesOrderService.findByCustomer(customerId);
        } else {
            result = salesOrderService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrder>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.findById(id)));
    }




    @PostMapping
    public ResponseEntity<ApiResponse<SalesOrder>> create(@Valid @RequestBody SalesOrder order) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sales order created", salesOrderService.create(order)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SalesOrder>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SalesOrder.Status status = SalesOrder.Status.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Sales order status updated",
                salesOrderService.updateStatus(id, status)));
    }


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        salesOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Sales order deleted", null));
    }
}

