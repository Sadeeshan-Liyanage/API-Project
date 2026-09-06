package com.example.api_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TopProduct {
    private String name;
    private String sku;
    private int quantitySold;
    private BigDecimal revenue;
}

