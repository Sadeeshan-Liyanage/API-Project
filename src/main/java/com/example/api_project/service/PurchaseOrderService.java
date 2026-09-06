package com.example.api_project.service;


import com.example.api_project.entity.*;
import com.example.api_project.exception.BadRequestException;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.*;
import com.example.api_project.security.CustomUserDetails;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditLogger auditLogger;

    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    public List<PurchaseOrder> findByBranch(Long branchId) {
        return purchaseOrderRepository.findByBranchId(branchId);
    }





    @Transactional
    public PurchaseOrder create(PurchaseOrder po) {
        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new BadRequestException("A purchase order requires at least one item");
        }
        Supplier supplier = supplierRepository.findById(requireId(po.getSupplier(), "Supplier"))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", po.getSupplier().getId()));
        Branch branch = branchRepository.findById(requireId(po.getBranch(), "Branch"))
                .orElseThrow(() -> new ResourceNotFoundException("Branch", po.getBranch().getId()));

        po.setSupplier(supplier);
        po.setBranch(branch);
        po.setOrderedBy(currentUser());
        po.setStatus(PurchaseOrder.Status.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItem item : po.getItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                throw new BadRequestException("Each purchase order item requires a product id");
            }
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProduct().getId()));
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BadRequestException("Item quantity must be at least 1");
            }
            BigDecimal subtotal = item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setProduct(product);
            item.setSubtotal(subtotal);
            item.setPurchaseOrder(po);
            total = total.add(subtotal);
        }
        po.setTotalAmount(total);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogger.log("CREATE", "PurchaseOrder", saved.getId(), "total=" + total + " items=" + po.getItems().size());
        return saved;
    }




    @Transactional
    public PurchaseOrder updateStatus(Long id, PurchaseOrder.Status newStatus) {
        PurchaseOrder po = findById(id);

        if (po.getStatus() == PurchaseOrder.Status.RECEIVED) {
            throw new BadRequestException("Purchase order already marked as RECEIVED");
        }
        if (po.getStatus() == PurchaseOrder.Status.CANCELLED) {
            throw new BadRequestException("Cannot change status of a cancelled purchase order");
        }

        po.setStatus(newStatus);

        if (newStatus == PurchaseOrder.Status.RECEIVED) {
            for (PurchaseOrderItem item : po.getItems()) {
                Inventory inv = inventoryRepository
                        .findByProductIdAndBranchId(item.getProduct().getId(), po.getBranch().getId())
                        .orElseGet(() -> inventoryRepository.save(Inventory.builder()
                                .product(item.getProduct())
                                .branch(po.getBranch())
                                .quantity(0)
                                .build()));
                inv.setQuantity(inv.getQuantity() + item.getQuantity());
                inventoryRepository.save(inv);
            }
        }

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogger.log("STATUS_CHANGE", "PurchaseOrder", saved.getId(), "status=" + newStatus);
        return saved;
    }


    @Transactional
    public void delete(Long id) {
        PurchaseOrder po = findById(id);
        if (po.getStatus() == PurchaseOrder.Status.RECEIVED) {
            throw new BadRequestException("Cannot delete a received purchase order");
        }
        purchaseOrderRepository.delete(po);
        auditLogger.log("DELETE", "PurchaseOrder", id, "removed");
    }


    private Long requireId(Object entityRef, String name) {
        if (entityRef == null) throw new BadRequestException(name + " id is required");
        if (entityRef instanceof Supplier s) {
            if (s.getId() == null) throw new BadRequestException(name + " id is required");
            return s.getId();
        }
        if (entityRef instanceof Branch b) {
            if (b.getId() == null) throw new BadRequestException(name + " id is required");
            return b.getId();
        }
        throw new BadRequestException(name + " id is required");
    }


    private User currentUser() {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(cud.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", cud.getId()));
    }
}

