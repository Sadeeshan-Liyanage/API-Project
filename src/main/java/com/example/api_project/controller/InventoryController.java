package com.example.api_project.controller;

import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.Inventory;
import com.example.api_project.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Inventory>>> getAll(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Integer lowStockThreshold) {
        List<Inventory> result;
        if (lowStockThreshold != null) {
            result = inventoryService.findLowStock(lowStockThreshold);
        } else if (branchId != null) {
            result = inventoryService.findByBranch(branchId);
        } else {
            result = inventoryService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventory>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.findById(id)));
    }


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Inventory>> create(@Valid @RequestBody Inventory inventory) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory record created", inventoryService.create(inventory)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PatchMapping("/{id}/adjust")
    public ResponseEntity<ApiResponse<Inventory>> adjust(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int delta = body.getOrDefault("delta", 0);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", inventoryService.adjustQuantity(id, delta)));
    }


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventory>> setQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int quantity = body.getOrDefault("quantity", 0);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", inventoryService.setQuantity(id, quantity)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory record deleted", null));
    }
}



