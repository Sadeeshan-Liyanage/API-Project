package com.example.api_project.service;


import com.example.api_project.entity.Role;
import com.example.api_project.exception.DuplicateResourceException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.RoleRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final AuditLogger auditLogger;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }


    @Transactional
    public Role create(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new DuplicateResourceException("Role already exists: " + role.getName());
        }
        Role saved = roleRepository.save(role);
        auditLogger.log("CREATE", "Role", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public Role update(Long id, Role updated) {
        Role role = findById(id);
        role.setName(updated.getName());
        role.setDescription(updated.getDescription());
        Role saved = roleRepository.save(role);
        auditLogger.log("UPDATE", "Role", saved.getId(), saved.getName());
        return saved;
    }



    @Transactional
    public void delete(Long id) {
        Role role = findById(id);
        roleRepository.delete(role);
        auditLogger.log("DELETE", "Role", id, role.getName());
    }
}



