package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.Branch;
import com.example.api_project.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Branch>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(branchService.findAll()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.findById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Branch>> create(@Valid @RequestBody Branch branch) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created", branchService.create(branch)));
    }


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> update(@PathVariable Long id, @Valid @RequestBody Branch branch) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", branchService.update(id, branch)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Branch>> patchStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ResponseEntity.ok(ApiResponse.success("Branch status updated", branchService.patchStatus(id, active)));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted", null));
    }
}

