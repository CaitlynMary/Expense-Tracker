package com.familyexpensetracker.module.grocery.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroceryItemResponse {
    private Long id;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal estimatedPrice;
    private Boolean isPurchased;
    private String status;
    private LocalDateTime purchasedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
