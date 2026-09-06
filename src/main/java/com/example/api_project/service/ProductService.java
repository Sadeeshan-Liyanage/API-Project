package com.example.api_project.service;


import com.example.api_project.entity.Product;
import com.example.api_project.entity.SubCategory;
import com.example.api_project.entity.Supplier;
import com.example.api_project.exception.DuplicateResourceException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.ProductRepository;
import com.example.api_project.repository.SubCategoryRepository;
import com.example.api_project.repository.SupplierRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final AuditLogger auditLogger;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }


    public List<Product> findBySubCategory(Long subCategoryId) {
        return productRepository.findBySubCategoryId(subCategoryId);
    }



    @Transactional
    public Product create(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new DuplicateResourceException("Product SKU already exists: " + product.getSku());
        }
        product.setSubCategory(resolveSubCategory(product));
        product.setSupplier(resolveSupplier(product));
        Product saved = productRepository.save(product);
        auditLogger.log("CREATE", "Product", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Product update(Long id, Product updated) {
        Product product = findById(id);
        product.setName(updated.getName());
        product.setUnitPrice(updated.getUnitPrice());
        product.setUnit(updated.getUnit());
        if (updated.getReorderLevel() != null) product.setReorderLevel(updated.getReorderLevel());
        if (updated.getSubCategory() != null && updated.getSubCategory().getId() != null) {
            product.setSubCategory(resolveSubCategory(updated));
        }
        if (updated.getSupplier() != null && updated.getSupplier().getId() != null) {
            product.setSupplier(resolveSupplier(updated));
        }
        if (updated.getActive() != null) product.setActive(updated.getActive());
        Product saved = productRepository.save(product);
        auditLogger.log("UPDATE", "Product", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public Product patchActive(Long id, boolean active) {
        Product product = findById(id);
        product.setActive(active);
        Product saved = productRepository.save(product);
        auditLogger.log("PATCH", "Product", saved.getId(), "active=" + active);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
        auditLogger.log("DELETE", "Product", id, product.getName());
    }

    private SubCategory resolveSubCategory(Product product) {
        if (product.getSubCategory() == null || product.getSubCategory().getId() == null) {
            throw new ResourceNotFoundException("SubCategory id is required for a product");
        }
        return subCategoryRepository.findById(product.getSubCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", product.getSubCategory().getId()));
    }


    private Supplier resolveSupplier(Product product) {
        if (product.getSupplier() == null || product.getSupplier().getId() == null) {
            return null;
        }
        return supplierRepository.findById(product.getSupplier().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", product.getSupplier().getId()));
    }
}


