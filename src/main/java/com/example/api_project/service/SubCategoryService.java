package com.example.api_project.service;


import com.example.api_project.entity.Category;
import com.example.api_project.entity.SubCategory;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.CategoryRepository;
import com.example.api_project.repository.SubCategoryRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogger auditLogger;

    public List<SubCategory> findAll() {
        return subCategoryRepository.findAll();
    }

    public List<SubCategory> findByCategory(Long categoryId) {
        return subCategoryRepository.findByCategoryId(categoryId);
    }

    public SubCategory findById(Long id) {
        return subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", id));
    }

    @Transactional
    public SubCategory create(SubCategory subCategory) {
        Category category = resolveCategory(subCategory);
        subCategory.setCategory(category);
        SubCategory saved = subCategoryRepository.save(subCategory);
        auditLogger.log("CREATE", "SubCategory", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public SubCategory update(Long id, SubCategory updated) {
        SubCategory subCategory = findById(id);
        subCategory.setName(updated.getName());
        if (updated.getCategory() != null && updated.getCategory().getId() != null) {
            subCategory.setCategory(resolveCategory(updated));
        }
        SubCategory saved = subCategoryRepository.save(subCategory);
        auditLogger.log("UPDATE", "SubCategory", saved.getId(), saved.getName());
        return saved;
    }



    @Transactional
    public void delete(Long id) {
        SubCategory subCategory = findById(id);
        subCategoryRepository.delete(subCategory);
        auditLogger.log("DELETE", "SubCategory", id, subCategory.getName());
    }


    private Category resolveCategory(SubCategory subCategory) {
        if (subCategory.getCategory() == null || subCategory.getCategory().getId() == null) {
            throw new ResourceNotFoundException("Category id is required for a sub-category");
        }
        return categoryRepository.findById(subCategory.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", subCategory.getCategory().getId()));
    }
}


