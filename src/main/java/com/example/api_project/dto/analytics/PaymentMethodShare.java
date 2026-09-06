package com.example.api_project.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;


import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentMethodShare {
    private String method;
    private int count;
    private BigDecimal total;
}


