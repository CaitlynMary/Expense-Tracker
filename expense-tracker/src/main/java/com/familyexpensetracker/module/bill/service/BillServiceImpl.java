package com.familyexpensetracker.module.bill.service;

import com.familyexpensetracker.common.mapper.BillMapper;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.bill.dto.BillRequest;
import com.familyexpensetracker.module.bill.dto.BillResponse;
import com.familyexpensetracker.module.bill.entity.Bill;
import com.familyexpensetracker.module.bill.entity.BillFrequency;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;

    @Override
    @Transactional
    public BillResponse createBill(BillRequest request) {
        Bill bill = billMapper.toEntity(request);
        Bill savedBill = billRepository.save(bill);
        return billMapper.toResponse(savedBill);
    }

    @Override
    @Transactional
    public BillResponse updateBill(Long id, BillRequest request) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        billMapper.updateEntity(bill, request);
        Bill updatedBill = billRepository.save(bill);
        return billMapper.toResponse(updatedBill);
    }

    @Override
    @Transactional
    public void deleteBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        billRepository.delete(bill);
    }

    @Override
    @Transactional
    public BillResponse markBillAsPaid(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        bill.setPaidDate(LocalDate.now());
        if (bill.getFrequency() == BillFrequency.ONCE) {
            bill.setStatus(BillStatus.PAID);
        } else {
            bill.setDueDate(nextDueDate(bill.getDueDate(), bill.getFrequency()));
            bill.setStatus(bill.getDueDate().isBefore(LocalDate.now()) ? BillStatus.OVERDUE : BillStatus.PENDING);
        }
        Bill savedBill = billRepository.save(bill);
        return billMapper.toResponse(savedBill);
    }

    @Override
    @Transactional
    public List<BillResponse> getAllBills() {
        List<Bill> bills = billRepository.findAllByOrderByDueDateAsc();
        checkAndUpdateOverdue(bills);
        return bills.stream()
                .filter(bill -> !(bill.getFrequency() == BillFrequency.ONCE && bill.getStatus() == BillStatus.PAID))
                .map(billMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BillResponse> getUpcomingBills() {
        // Upcoming bills are unpaid bills with due dates starting from today
        List<Bill> bills = billRepository.findByStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(
                BillStatus.PAID, LocalDate.now());
        // Dynamic check in case overdue bills were not updated yet
        checkAndUpdateOverdue(bills);
        return bills.stream()
                .map(billMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BillResponse> getOverdueBills() {
        // Overdue bills are unpaid bills whose due date is in the past
        List<Bill> bills = billRepository.findByStatusNotAndDueDateLessThanOrderByDueDateAsc(
                BillStatus.PAID, LocalDate.now());
        checkAndUpdateOverdue(bills);
        return bills.stream()
                .map(billMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BillResponse getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        checkAndUpdateOverdue(bill);
        return billMapper.toResponse(bill);
    }

    /**
     * Checks list of bills and dynamically marks active/pending bills whose due date has passed as OVERDUE.
     */
    private void checkAndUpdateOverdue(List<Bill> bills) {
        LocalDate today = LocalDate.now();
        boolean needsSave = false;
        for (Bill bill : bills) {
            if (bill.getStatus() == BillStatus.PENDING && bill.getDueDate().isBefore(today)) {
                bill.setStatus(BillStatus.OVERDUE);
                needsSave = true;
            }
        }
        if (needsSave) {
            billRepository.saveAll(bills);
        }
    }

    /**
     * Checks a single bill and dynamically marks active/pending bill whose due date has passed as OVERDUE.
     */
    private void checkAndUpdateOverdue(Bill bill) {
        if (bill.getStatus() == BillStatus.PENDING && bill.getDueDate().isBefore(LocalDate.now())) {
            bill.setStatus(BillStatus.OVERDUE);
            billRepository.save(bill);
        }
    }

    private LocalDate nextDueDate(LocalDate currentDueDate, BillFrequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDueDate.plusDays(1);
            case WEEKLY -> currentDueDate.plusWeeks(1);
            case BIWEEKLY -> currentDueDate.plusWeeks(2);
            case MONTHLY -> currentDueDate.plusMonths(1);
            case EVERY_2_MONTHS, EVERY_TWO_MONTHS -> currentDueDate.plusMonths(2);
            case QUARTERLY -> currentDueDate.plusMonths(3);
            case YEARLY -> currentDueDate.plusYears(1);
            case ONCE -> currentDueDate;
        };
    }
}
