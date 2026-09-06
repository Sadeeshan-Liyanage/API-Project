package com.example.api_project.service;


import com.example.api_project.entity.Supplier;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.SupplierRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogger auditLogger;

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }


    @Transactional
    public Supplier create(Supplier supplier) {
        Supplier saved = supplierRepository.save(supplier);
        auditLogger.log("CREATE", "Supplier", saved.getId(), saved.getName());
        return saved;
    }



    @Transactional
    public Supplier update(Long id, Supplier updated) {
        Supplier supplier = findById(id);
        supplier.setName(updated.getName());
        supplier.setContactPerson(updated.getContactPerson());
        supplier.setPhone(updated.getPhone());
        supplier.setEmail(updated.getEmail());
        supplier.setAddress(updated.getAddress());
        Supplier saved = supplierRepository.save(supplier);
        auditLogger.log("UPDATE", "Supplier", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public void delete(Long id) {
        Supplier supplier = findById(id);
        supplierRepository.delete(supplier);
        auditLogger.log("DELETE", "Supplier", id, supplier.getName());
    }
}



