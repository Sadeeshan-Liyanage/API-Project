package com.example.api_project.service;


import com.example.api_project.entity.Branch;
import com.example.api_project.entity.Inventory;
import com.example.api_project.entity.Product;
import com.example.api_project.exception.BadRequestException;
import com.example.api_project.exception.DuplicateResourceException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.BranchRepository;
import com.example.api_project.repository.InventoryRepository;
import com.example.api_project.repository.ProductRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final AuditLogger auditLogger;

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }


    public List<Inventory> findByBranch(Long branchId) {
        return inventoryRepository.findByBranchId(branchId);
    }

    public List<Inventory> findLowStock(int threshold) {
        return inventoryRepository.findByQuantityLessThan(threshold);
    }

    @Transactional
    public Inventory create(Inventory inventory) {
        if (inventory.getProduct() == null || inventory.getProduct().getId() == null) {
            throw new BadRequestException("Product id is required");
        }
        if (inventory.getBranch() == null || inventory.getBranch().getId() == null) {
            throw new BadRequestException("Branch id is required");
        }
        Product product = productRepository.findById(inventory.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", inventory.getProduct().getId()));
        Branch branch = branchRepository.findById(inventory.getBranch().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", inventory.getBranch().getId()));

        if (inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId()).isPresent()) {
            throw new DuplicateResourceException("Inventory record already exists for this product at this branch");
        }

        inventory.setProduct(product);
        inventory.setBranch(branch);
        if (inventory.getQuantity() == null) inventory.setQuantity(0);
        Inventory saved = inventoryRepository.save(inventory);
        auditLogger.log("CREATE", "Inventory", saved.getId(),
                "product=" + product.getSku() + " branch=" + branch.getName() + " qty=" + saved.getQuantity());
        return saved;
    }


    @Transactional
    public Inventory adjustQuantity(Long id, int delta) {
        Inventory inventory = findById(id);
        int newQty = inventory.getQuantity() + delta;
        if (newQty < 0) {
            throw new BadRequestException("Insufficient stock: cannot reduce below zero");
        }
        inventory.setQuantity(newQty);
        Inventory saved = inventoryRepository.save(inventory);
        auditLogger.log("ADJUST", "Inventory", saved.getId(), "delta=" + delta + " newQty=" + newQty);
        return saved;
    }



    @Transactional
    public Inventory setQuantity(Long id, int quantity) {
        if (quantity < 0) throw new BadRequestException("Quantity cannot be negative");
        Inventory inventory = findById(id);
        inventory.setQuantity(quantity);
        Inventory saved = inventoryRepository.save(inventory);
        auditLogger.log("UPDATE", "Inventory", saved.getId(), "qty=" + quantity);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Inventory inventory = findById(id);
        inventoryRepository.delete(inventory);
        auditLogger.log("DELETE", "Inventory", id, "removed");
    }
}



