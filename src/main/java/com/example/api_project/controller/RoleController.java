package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.Role;
import com.example.api_project.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(roleService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.findById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Role>> create(@Valid @RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created", roleService.create(role)));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> update(@PathVariable Long id, @Valid @RequestBody Role role) {
        return ResponseEntity.ok(ApiResponse.success("Role updated", roleService.update(id, role)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted", null));
    }
}


