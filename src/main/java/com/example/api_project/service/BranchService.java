package com.example.api_project.service;


import com.example.api_project.entity.Branch;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.BranchRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final AuditLogger auditLogger;

    public List<Branch> findAll() {
        return branchRepository.findAll();
    }

    public Branch findById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
    }

    @Transactional
    public Branch create(Branch branch) {
        Branch saved = branchRepository.save(branch);
        auditLogger.log("CREATE", "Branch", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Branch update(Long id, Branch updated) {
        Branch branch = findById(id);
        branch.setName(updated.getName());
        branch.setAddress(updated.getAddress());
        branch.setPhone(updated.getPhone());
        if (updated.getActive() != null) branch.setActive(updated.getActive());
        Branch saved = branchRepository.save(branch);
        auditLogger.log("UPDATE", "Branch", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public Branch patchStatus(Long id, boolean active) {
        Branch branch = findById(id);
        branch.setActive(active);
        Branch saved = branchRepository.save(branch);
        auditLogger.log("PATCH", "Branch", saved.getId(), "active=" + active);
        return saved;
    }



    @Transactional
    public void delete(Long id) {
        Branch branch = findById(id);
        branchRepository.delete(branch);
        auditLogger.log("DELETE", "Branch", id, branch.getName());
    }
}


