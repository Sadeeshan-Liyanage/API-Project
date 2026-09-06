package com.example.api_project.service;

import com.example.api_project.entity.Category;
import com.example.api_project.exception.DuplicateResourceException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.util.AuditLogger;
import com.example.api_project.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogger auditLogger;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new DuplicateResourceException("Category already exists: " + category.getName());
        }
        Category saved = categoryRepository.save(category);
        auditLogger.log("CREATE", "Category", saved.getId(), saved.getName());
        return saved;
    }



    @Transactional
    public Category update(Long id, Category updated) {
        Category category = findById(id);
        category.setName(updated.getName());
        category.setDescription(updated.getDescription());
        Category saved = categoryRepository.save(category);
        auditLogger.log("UPDATE", "Category", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
        auditLogger.log("DELETE", "Category", id, category.getName());
    }
}







