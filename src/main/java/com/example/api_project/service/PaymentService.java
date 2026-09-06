package com.example.api_project.service;


import com.example.api_project.entity.Payment;
import com.example.api_project.entity.SalesOrder;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.PaymentRepository;
import com.example.api_project.repository.SalesOrderRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final AuditLogger auditLogger;

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }


    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }


    public List<Payment> findBySalesOrder(Long salesOrderId) {
        return paymentRepository.findBySalesOrderId(salesOrderId);
    }




    @Transactional
    public Payment create(Payment payment) {
        if (payment.getSalesOrder() == null || payment.getSalesOrder().getId() == null) {
            throw new Bad

            RequestException("A sales order id is required for a payment");
        }
        SalesOrder order = salesOrderRepository.findById(payment.getSalesOrder().getId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", payment.getSalesOrder().getId()));

        if (order.getStatus() == SalesOrder.Status.CANCELLED) {
            throw new BadRequestException("Cannot record a payment against a cancelled order");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        payment.setSalesOrder(order);
        if (payment.getStatus() == null) {
            payment.setStatus(Payment.Status.SUCCESS);
        }
        Payment saved = paymentRepository.save(payment);

        if (payment.getStatus() == Payment.Status.SUCCESS) {
            BigDecimal totalPaid = paymentRepository.findBySalesOrderId(order.getId()).stream()
                    .filter(p -> p.getStatus() == Payment.Status.SUCCESS)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPaid.compareTo(order.getTotalAmount()) >= 0
                    && order.getStatus() == SalesOrder.Status.PENDING) {
                order.setStatus(SalesOrder.Status.COMPLETED);
                salesOrderRepository.save(order);
            }
        }


        auditLogger.log("CREATE", "Payment", saved.getId(),
                "order=" + order.getId() + " amount=" + saved.getAmount() + " method=" + saved.getMethod());
        return saved;
    }


    @Transactional
    public void delete(Long id) {
        Payment payment = findById(id);
        paymentRepository.delete(payment);
        auditLogger.log("DELETE", "Payment", id, "removed");
    }
}


