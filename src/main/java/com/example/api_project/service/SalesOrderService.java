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
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public List<SalesOrder> findAll() {
        return salesOrderRepository.findAll();
    }

    public SalesOrder findById(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
    }


    public List<SalesOrder> findByBranch(Long branchId) {
        return salesOrderRepository.findByBranchId(branchId);
    }

    public List<SalesOrder> findByCustomer(Long customerId) {
        return salesOrderRepository.findByCustomerId(customerId);
    }




    @Transactional
    public SalesOrder create(SalesOrder order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("A sales order requires at least one item");
        }
        if (order.getBranch() == null || order.getBranch().getId() == null) {
            throw new BadRequestException("Branch id is required");
        }
        Branch branch = branchRepository.findById(order.getBranch().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", order.getBranch().getId()));

        Customer customer = null;
        if (order.getCustomer() != null && order.getCustomer().getId() != null) {
            customer = customerRepository.findById(order.getCustomer().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", order.getCustomer().getId()));
        }

        Discount discount = null;
        if (order.getDiscount() != null && order.getDiscount().getId() != null) {
            discount = discountRepository.findById(order.getDiscount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Discount", order.getDiscount().getId()));
            if (!Boolean.TRUE.equals(discount.getActive())) {
                throw new BadRequestException("Discount code is not active: " + discount.getCode());
            }
        }



        order.setBranch(branch);
        order.setCustomer(customer);
        order.setDiscount(discount);
        order.setCashier(currentUser());
        order.setStatus(SalesOrder.Status.PENDING);


        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderItem item : order.getItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                throw new BadRequestException("Each sales order item requires a product id");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BadRequestException("Item quantity must be at least 1");
            }
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProduct().getId()));

            Inventory inventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())
                    .orElseThrow(() -> new BadRequestException(
                            "No stock record for product " + product.getSku() + " at this branch"));
            if (inventory.getQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product " + product.getSku()
                        + " (available: " + inventory.getQuantity() + ")");
            }
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);

            BigDecimal unitPrice = product.getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            item.setProduct(product);
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            item.setSalesOrder(order);

            total = total.add(subtotal);
        }


        if (discount != null) {
            total = applyDiscount(total, discount);
        }
        order.setTotalAmount(total);

        SalesOrder saved = salesOrderRepository.save(order);
        auditLogger.log("CREATE", "SalesOrder", saved.getId(),
                "total=" + total + " items=" + order.getItems().size());
        return saved;
    }




    @Transactional
    public SalesOrder updateStatus(Long id, SalesOrder.Status newStatus) {
        SalesOrder order = findById(id);

        if ((newStatus == SalesOrder.Status.CANCELLED || newStatus == SalesOrder.Status.REFUNDED)
                && order.getStatus() != SalesOrder.Status.CANCELLED
                && order.getStatus() != SalesOrder.Status.REFUNDED) {


            for (SalesOrderItem item : order.getItems()) {
                Inventory inv = inventoryRepository
                        .findByProductIdAndBranchId(item.getProduct().getId(), order.getBranch().getId())
                        .orElse(null);
                if (inv != null) {
                    inv.setQuantity(inv.getQuantity() + item.getQuantity());
                    inventoryRepository.save(inv);
                }
            }
        }

        order.setStatus(newStatus);
        SalesOrder saved = salesOrderRepository.save(order);
        auditLogger.log("STATUS_CHANGE", "SalesOrder", saved.getId(), "status=" + newStatus);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        SalesOrder order = findById(id);
        if (order.getStatus() == SalesOrder.Status.COMPLETED) {
            throw new BadRequestException("Cannot delete a completed sales order; cancel or refund it instead");
        }
        salesOrderRepository.delete(order);
        auditLogger.log("DELETE", "SalesOrder", id, "removed");
    }


    private BigDecimal applyDiscount(BigDecimal total, Discount discount) {
        if (discount.getType() == Discount.DiscountType.PERCENTAGE) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    discount.getValue().divide(BigDecimal.valueOf(100)));
            return total.multiply(factor).max(BigDecimal.ZERO);
        }
        return total.subtract(discount.getValue()).max(BigDecimal.ZERO);
    }



    private User currentUser() {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(cud.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", cud.getId()));
    }
}


