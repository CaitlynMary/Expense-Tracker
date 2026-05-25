package com.familyexpensetracker.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsReportResponse {

    private BigDecimal totalGoalAmount;
    private BigDecimal totalSavedAmount;
    private Double progressPercentage;
}