package com.example.api_project.controller;


import com.example.api_project.dto.ApiResponse;
import com.example.api_project.entity.AuditLog;
import com.example.api_project.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(auditLogRepository.findAll()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(auditLogRepository.findByUserId(userId)));
    }


    @GetMapping("/entity/{entityName}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByEntity(@PathVariable String entityName) {
        return ResponseEntity.ok(ApiResponse.success(auditLogRepository.findByEntityName(entityName)));
    }
}

