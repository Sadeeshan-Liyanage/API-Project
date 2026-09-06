package com.example.api_project.service;


import com.example.api_project.entity.Discount;
import com.example.api_project.exception.DuplicateResourceException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.DiscountRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final AuditLogger auditLogger;

    public List<Discount> findAll() {
        return discountRepository.findAll();
    }

    public Discount findById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", id));
    }


    @Transactional
    public Discount create(Discount discount) {
        if (discountRepository.existsByCodeIgnoreCase(discount.getCode())) {
            throw new DuplicateResourceException("Discount code already exists: " + discount.getCode());
        }
        Discount saved = discountRepository.save(discount);
        auditLogger.log("CREATE", "Discount", saved.getId(), saved.getCode());
        return saved;
    }



    @Transactional
    public Discount update(Long id, Discount updated) {
        Discount discount = findById(id);
        discount.setDescription(updated.getDescription());
        discount.setType(updated.getType());
        discount.setValue(updated.getValue());
        discount.setValidFrom(updated.getValidFrom());
        discount.setValidTo(updated.getValidTo());
        if (updated.getActive() != null) discount.setActive(updated.getActive());
        Discount saved = discountRepository.save(discount);
        auditLogger.log("UPDATE", "Discount", saved.getId(), saved.getCode());
        return saved;
    }



    @Transactional
    public void delete(Long id) {
        Discount discount = findById(id);
        discountRepository.delete(discount);
        auditLogger.log("DELETE", "Discount", id, discount.getCode());
    }
}

