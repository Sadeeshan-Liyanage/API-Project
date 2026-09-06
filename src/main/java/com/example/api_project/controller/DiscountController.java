package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.Discount;
import com.example.api_project.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Discount>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(discountService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Discount>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(discountService.findById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Discount>> create(@Valid @RequestBody Discount discount) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Discount created", discountService.create(discount)));
    }



    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Discount>> update(@PathVariable Long id, @Valid @RequestBody Discount discount) {
        return ResponseEntity.ok(ApiResponse.success("Discount updated", discountService.update(id, discount)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Discount deleted", null));
    }
}
