package com.example.api_project.dto.analytics;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@AllArgsConstructor
public class TopCustomer {
    private String name;
    private int orderCount;
    private BigDecimal totalSpent;
}

