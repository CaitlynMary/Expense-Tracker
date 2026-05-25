package com.familyexpensetracker.common.mapper;

import com.familyexpensetracker.module.bill.dto.BillRequest;
import com.familyexpensetracker.module.bill.dto.BillResponse;
import com.familyexpensetracker.module.bill.entity.Bill;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.entity.BillType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BillMapper {

    public Bill toEntity(BillRequest request) {
        BillStatus initialStatus = request.getStatus();
        if (initialStatus == null) {
            if (request.getPaidDate() != null) {
                initialStatus = BillStatus.PAID;
            } else if (request.getDueDate().isBefore(LocalDate.now())) {
                initialStatus = BillStatus.OVERDUE;
            } else {
                initialStatus = BillStatus.PENDING;
            }
        }

        return Bill.builder()
                .name(request.getName().trim())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .paidDate(request.getPaidDate())
                .status(initialStatus)
                .billType(request.getBillType() != null ? request.getBillType() : BillType.FIXED)
                .frequency(request.getFrequency())
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();
    }

    public BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .name(bill.getName())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .paidDate(bill.getPaidDate())
                .status(bill.getStatus())
                .billType(bill.getBillType() != null ? bill.getBillType() : BillType.FIXED)
                .frequency(bill.getFrequency())
                .notes(bill.getNotes())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }

    public void updateEntity(Bill bill, BillRequest request) {
        bill.setName(request.getName().trim());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());
        bill.setPaidDate(request.getPaidDate());
        bill.setBillType(request.getBillType() != null ? request.getBillType() : BillType.FIXED);
        bill.setFrequency(request.getFrequency());
        bill.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        if (request.getStatus() != null) {
            bill.setStatus(request.getStatus());
        } else {
            // Re-evaluate status based on updated dates if not explicitly set
            if (bill.getPaidDate() != null) {
                bill.setStatus(BillStatus.PAID);
            } else if (bill.getDueDate().isBefore(LocalDate.now())) {
                bill.setStatus(BillStatus.OVERDUE);
            } else {
                bill.setStatus(BillStatus.PENDING);
            }
        }
    }
}
