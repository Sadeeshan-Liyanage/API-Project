package com.example.api_project.service;

import com.example.api_project.dto.analytics.*;
import com.example.api_project.entity.*;
import com.example.api_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SalesOrderRepository salesOrderRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public DashboardSummary getSummary() {
        List<SalesOrder> completed = salesOrderRepository.findByStatus(SalesOrder.Status.COMPLETED);
        LocalDate today = LocalDate.now();

        BigDecimal allTime = completed.stream()
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayRevenue = completed.stream()
                .filter(o -> o.getOrderDate().toLocalDate().equals(today))
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long todayOrders = completed.stream()
                .filter(o -> o.getOrderDate().toLocalDate().equals(today))
                .count();


        long lowStock = inventoryRepository.findAll().stream()
                .filter(i -> i.getQuantity() <= i.getProduct().getReorderLevel())
                .count();


        long pendingPOs = purchaseOrderRepository.findByStatus(PurchaseOrder.Status.PENDING).size()
                + purchaseOrderRepository.findByStatus(PurchaseOrder.Status.APPROVED).size();

        return new DashboardSummary(allTime, todayRevenue, todayOrders, lowStock, pendingPOs);
    }



    public List<RevenuePoint> getRevenueTrend(int days) {
        List<SalesOrder> completed = salesOrderRepository.findByStatus(SalesOrder.Status.COMPLETED);
        LocalDate start = LocalDate.now().minusDays(days - 1L);

        Map<LocalDate, BigDecimal> byDate = new TreeMap<>();
        for (int i = 0; i < days; i++) byDate.put(start.plusDays(i), BigDecimal.ZERO);

        for (SalesOrder o : completed) {
            LocalDate d = o.getOrderDate().toLocalDate();
            if (!d.isBefore(start)) {
                byDate.merge(d, o.getTotalAmount(), BigDecimal::add);
            }

        }

        return byDate.entrySet().stream()
                .map(e -> new RevenuePoint(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());
    }


    public List<TopProduct> getTopProducts(int limit) {
        List<SalesOrder> completed = salesOrderRepository.findByStatus(SalesOrder.Status.COMPLETED);
        Map<Long, TopProductAcc> acc = new HashMap<>();

        for (SalesOrder o : completed) {
            for (SalesOrderItem item : o.getItems()) {
                Product p = item.getProduct();
                TopProductAcc a = acc.computeIfAbsent(p.getId(), k -> new TopProductAcc(p.getName(), p.getSku()));
                a.quantitySold += item.getQuantity();
                a.revenue = a.revenue.add(item.getSubtotal());
            }
        }

        return acc.values().stream()
                .sorted((a, b) -> Integer.compare(b.quantitySold, a.quantitySold))
                .limit(limit)
                .map(a -> new TopProduct(a.name, a.sku, a.quantitySold, a.revenue))
                .collect(Collectors.toList());
    }

    public List<TopCustomer> getTopCustomers(int limit) {
        List<SalesOrder> completed = salesOrderRepository.findByStatus(SalesOrder.Status.COMPLETED);
        Map<Long, TopCustomerAcc> acc = new HashMap<>();

        for (SalesOrder o : completed) {
            if (o.getCustomer() == null) continue;
            Customer c = o.getCustomer();
            TopCustomerAcc a = acc.computeIfAbsent(c.getId(), k -> new TopCustomerAcc(c.getName()));
            a.orderCount++;
            a.totalSpent = a.totalSpent.add(o.getTotalAmount());
        }

        return acc.values().stream()
                .sorted((a, b) -> b.totalSpent.compareTo(a.totalSpent))
                .limit(limit)
                .map(a -> new TopCustomer(a.name, a.orderCount, a.totalSpent))
                .collect(Collectors.toList());
    }


    public List<PaymentMethodShare> getPaymentBreakdown() {
        Map<Payment.Method, PaymentAcc> acc = new EnumMap<>(Payment.Method.class);

        paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.Status.SUCCESS)
                .forEach(p -> {
                    PaymentAcc a = acc.computeIfAbsent(p.getMethod(), k -> new PaymentAcc());
                    a.count++;
                    a.total = a.total.add(p.getAmount());
                });

        return acc.entrySet().stream()
                .map(e -> new PaymentMethodShare(e.getKey().name(), e.getValue().count, e.getValue().total))
                .collect(Collectors.toList());
    }



    private static class TopProductAcc {
        String name, sku;
        int quantitySold = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        TopProductAcc(String name, String sku) { this.name = name; this.sku = sku; }
    }

    private static class TopCustomerAcc {
        String name;
        int orderCount = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;
        TopCustomerAcc(String name) { this.name = name; }
    }


    private static class PaymentAcc {
        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
    }
}



