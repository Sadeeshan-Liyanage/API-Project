package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.PurchaseOrder;
import com.example.api_project.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getAll(
            @RequestParam(required = false) Long branchId) {
        List<PurchaseOrder> result = branchId != null
                ? purchaseOrderService.findByBranch(branchId)
                : purchaseOrderService.findAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrder>> create(@Valid @RequestBody PurchaseOrder po) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created", purchaseOrderService.create(po)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PurchaseOrder>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        PurchaseOrder.Status status = PurchaseOrder.Status.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Purchase order status updated",
                purchaseOrderService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order deleted", null));
    }
}


