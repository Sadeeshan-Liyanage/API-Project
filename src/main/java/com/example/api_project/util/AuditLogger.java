package com.example.api_project.util;


import com.example.api_project.entity.AuditLog;
import com.example.api_project.entity.User;
import com.example.api_project.repository.AuditLogRepository;
import com.example.api_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogger {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityName, Long entityId, String details) {
        try {
            Long currentUserId = currentUserId().orElse(null);
            AuditLog entry = AuditLog.builder()
                    .user(currentUserId != null ? refUser(currentUserId) : null)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .build();
            auditLogRepository.save(entry);
            log.info("AUDIT [{}] {} #{} - {}", action, entityName, entityId, details);
        } catch (Exception e) {

            log.warn("Failed to write audit log: {}", e.getMessage());
        }
    }

    private Optional<Long> currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return Optional.ofNullable(cud.getId());
        }
        return Optional.empty();
    }

    private User refUser(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }
}


