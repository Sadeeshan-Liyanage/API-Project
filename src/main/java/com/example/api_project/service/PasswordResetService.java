package com.example.api_project.service;

import com.example.api_project.entity.User;
import com.example.api_project.exception.BadRequestException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.UserRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles "forgot password": emails a 6-digit code that expires in 10
 * minutes, then verifies it before letting the person set a new password.
 * Codes are kept in memory (not the database) — simple and sufficient
 * for a short-lived, one-time code.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long CODE_VALID_MINUTES = 10;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;

    private final Map<String, ResetEntry> pendingResets = new ConcurrentHashMap<>();

    public void requestReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Don't reveal whether the email exists — just log it and return quietly.
            log.info("Password reset requested for unknown email: {}", email);
            return;
        }
        String code = generateCode();
        pendingResets.put(email.toLowerCase(), new ResetEntry(code, Instant.now().plusSeconds(CODE_VALID_MINUTES * 60)));
        emailService.sendPasswordResetEmail(user, code);
        log.info("Password reset code sent to {}", email);
    }

    @Transactional
    public void confirmReset(String email, String code, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        ResetEntry entry = pendingResets.get(email.toLowerCase());
        if (entry == null || !entry.code.equals(code)) {
            throw new BadRequestException("Invalid or expired reset code");
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            pendingResets.remove(email.toLowerCase());
            throw new BadRequestException("This reset code has expired — request a new one");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        pendingResets.remove(email.toLowerCase());

        auditLogger.log("PASSWORD_RESET", "User", user.getId(), "password reset via forgot-password flow");
        log.info("Password reset completed for {}", email);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private record ResetEntry(String code, Instant expiresAt) {}
}

