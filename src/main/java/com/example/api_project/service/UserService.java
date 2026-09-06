package com.example.api_project.service;


import com.example.api_project.entity.Branch;
import com.example.api_project.entity.Role;
import com.example.api_project.entity.User;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.BranchRepository;
import com.example.api_project.repository.RoleRepository;
import com.example.api_project.repository.UserRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final AuditLogger auditLogger;

    public List<User> findAll() {
        return userRepository.findAll();
    }


    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }



    @Transactional
    public User update(Long id, User updated) {
        User user = findById(id);
        user.setFullName(updated.getFullName());
        user.setPhone(updated.getPhone());
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getRole() != null && updated.getRole().getId() != null) {
            Role role = roleRepository.findById(updated.getRole().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", updated.getRole().getId()));
            user.setRole(role);
        }
        if (updated.getBranch() != null && updated.getBranch().getId() != null) {
            Branch branch = branchRepository.findById(updated.getBranch().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", updated.getBranch().getId()));
            user.setBranch(branch);
        }
        User saved = userRepository.save(user);
        auditLogger.log("UPDATE", "User", saved.getId(), saved.getUsername());
        return saved;
    }



    @Transactional
    public User setEnabled(Long id, boolean enabled) {
        User user = findById(id);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditLogger.log("PATCH", "User", saved.getId(), "enabled=" + enabled);
        return saved;
    }


    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        auditLogger.log("DELETE", "User", id, user.getUsername());
    }
}



