package com.familyexpensetracker.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO representing spending per category.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReportResponse {
    private String category;
    private BigDecimal amount;
}
