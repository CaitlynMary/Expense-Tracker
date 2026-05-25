package com.familyexpensetracker.module.expense.service;

import com.familyexpensetracker.exception.BadRequestException;
import com.familyexpensetracker.module.expense.dto.ExpenseRequest;
import com.familyexpensetracker.module.expense.dto.ExpenseResponse;
import com.familyexpensetracker.module.expense.entity.Expense;
import com.familyexpensetracker.module.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private static final int DUPLICATE_GUARD_SECONDS = 5;

    @Override
    public ExpenseResponse addExpense(ExpenseRequest request) {
        log.info("ExpenseService.addExpense called title='{}' amount={} category='{}' expenseDate={}",
                request.getTitle(), request.getAmount(), request.getCategory(), request.getExpenseDate());

        enforceDuplicateGuard(request);

        Expense expense = mapToEntity(request);
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense persisted with id={}", savedExpense.getId());
        return mapToResponse(savedExpense);
    }

    @Override
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        existingExpense.setTitle(request.getTitle());
        existingExpense.setAmount(request.getAmount());
        existingExpense.setCategory(request.getCategory());
        existingExpense.setPaymentMethod(request.getPaymentMethod());
        existingExpense.setExpenseDate(request.getExpenseDate());
        existingExpense.setNotes(request.getNotes());

        Expense updatedExpense = expenseRepository.save(existingExpense);
        return mapToResponse(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        expenseRepository.delete(existingExpense);
    }

    @Override
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        return mapToResponse(expense);
    }

    @Override
    public Page<ExpenseResponse> getAllExpenses(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return expenseRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ExpenseResponse> filterExpensesByCategory(String category, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return expenseRepository.findByCategory(category, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ExpenseResponse> filterExpensesByDateRange(LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return expenseRepository.findByExpenseDateBetween(startDate, endDate, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ExpenseResponse> filterExpensesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return expenseRepository.findByAmountBetween(minAmount, maxAmount, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ExpenseResponse> searchExpenseByTitle(String title, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return expenseRepository.findByTitleContainingIgnoreCase(title, pageable).map(this::mapToResponse);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(sortBy).ascending() : 
                Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private void enforceDuplicateGuard(ExpenseRequest request) {
        LocalDateTime duplicateWindowStart = LocalDateTime.now().minusSeconds(DUPLICATE_GUARD_SECONDS);
        boolean duplicateExists = expenseRepository.existsByTitleIgnoreCaseAndAmountAndCategoryIgnoreCaseAndExpenseDateAndCreatedAtAfter(
                request.getTitle().trim(),
                request.getAmount(),
                request.getCategory().trim(),
                request.getExpenseDate(),
                duplicateWindowStart
        );

        if (duplicateExists) {
            log.warn("Blocked duplicate expense create title='{}' amount={} category='{}' expenseDate={} within {} seconds",
                    request.getTitle(), request.getAmount(), request.getCategory(), request.getExpenseDate(), DUPLICATE_GUARD_SECONDS);
            throw new BadRequestException("Duplicate expense submission detected. Please wait a moment before trying again.");
        }
    }

    private Expense mapToEntity(ExpenseRequest request) {
        return Expense.builder()
                .title(request.getTitle().trim())
                .amount(request.getAmount())
                .category(request.getCategory().trim())
                .paymentMethod(request.getPaymentMethod().trim())
                .expenseDate(request.getExpenseDate())
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .paymentMethod(expense.getPaymentMethod())
                .expenseDate(expense.getExpenseDate())
                .notes(expense.getNotes())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
