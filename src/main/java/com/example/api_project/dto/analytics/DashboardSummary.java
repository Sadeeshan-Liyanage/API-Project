package com.example.api_project.dto.analytics;


import lombok.AllArgsConstructor;
import lombok.Getter;



import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DashboardSummary {
    private BigDecimal allTimeRevenue;
    private BigDecimal todayRevenue;
    private long todayOrders;
    private long lowStockCount;
    private long pendingPurchaseOrders;
}

