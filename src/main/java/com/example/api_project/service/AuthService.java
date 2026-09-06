package com.example.api_project.service;


import com.example.api_project.dto.auth.AuthResponse;
import com.example.api_project.dto.auth.LoginRequest;
import com.example.api_project.dto.auth.RegisterRequest;
import com.example.api_project.entity.Branch;
import com.example.api_project.entity.Role;
import com.example.api_project.entity.User;
import com.example.api_project.exception.BadRequestException;
import com.example.api_project.exception.DuplicateResourceException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.BranchRepository;
import com.example.api_project.repository.RoleRepository;
import com.example.api_project.repository.UserRepository;
import com.example.api_project.security.JwtUtil;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        String roleName = (request.getRoleName() == null || request.getRoleName().isBlank())
                ? "ROLE_CASHIER" : request.getRoleName().toUpperCase();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Unknown role: " + roleName));

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
        }


        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .enabled(true)
                .role(role)
                .branch(branch)
                .build();

        User saved = userRepository.save(user);
        auditLogger.log("REGISTER", "User", saved.getId(), "New user registered: " + saved.getUsername());
        log.info("New user registered: {} with role {}", saved.getUsername(), role.getName());

        String token = jwtUtil.generateToken(saved.getUsername(), role.getName(), saved.getId());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(saved.getId())
                .username(saved.getUsername())
                .role(role.getName())
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
    }


    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUsername()));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName(), user.getId());
        auditLogger.log("LOGIN", "User", user.getId(), "User logged in: " + user.getUsername());
        log.info("User logged in: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().getName())
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
    }
}
