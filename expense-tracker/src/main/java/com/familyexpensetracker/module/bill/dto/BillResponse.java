package com.familyexpensetracker.module.bill.dto;

import com.familyexpensetracker.module.bill.entity.BillFrequency;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.entity.BillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private BillStatus status;
    private BillType billType;
    private BillFrequency frequency;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
