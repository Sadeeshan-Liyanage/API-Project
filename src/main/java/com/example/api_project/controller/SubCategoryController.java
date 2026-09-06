package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.SubCategory;
import com.example.api_project.service.SubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sub-categories")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubCategory>>> getAll(
            @RequestParam(required = false) Long categoryId) {
        List<SubCategory> result = categoryId != null
                ? subCategoryService.findByCategory(categoryId)
                : subCategoryService.findAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubCategory>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(subCategoryService.findById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<SubCategory>> create(@Valid @RequestBody SubCategory subCategory) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sub-category created", subCategoryService.create(subCategory)));
    }


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubCategory>> update(@PathVariable Long id, @Valid @RequestBody SubCategory subCategory) {
        return ResponseEntity.ok(ApiResponse.success("Sub-category updated", subCategoryService.update(id, subCategory)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Sub-category deleted", null));
    }
}


