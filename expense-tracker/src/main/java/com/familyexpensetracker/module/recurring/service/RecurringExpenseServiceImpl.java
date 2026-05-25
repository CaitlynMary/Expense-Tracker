package com.familyexpensetracker.module.recurring.service;

import com.familyexpensetracker.module.recurring.dto.RecurringExpenseRequest;
import com.familyexpensetracker.module.recurring.dto.RecurringExpenseResponse;
import com.familyexpensetracker.module.recurring.entity.RecurringExpense;
import com.familyexpensetracker.module.recurring.entity.RecurringExpense.Frequency;
import com.familyexpensetracker.module.recurring.repository.RecurringExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RecurringExpenseService} handling business logic for recurring expenses.
 */
@Service
@Transactional
public class RecurringExpenseServiceImpl implements RecurringExpenseService {

    private final RecurringExpenseRepository repository;

    @Autowired
    public RecurringExpenseServiceImpl(RecurringExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    public RecurringExpenseResponse createRecurringExpense(RecurringExpenseRequest request) {
        // Prevent duplicate titles (optional business rule)
        if (repository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Recurring expense with the same title already exists");
        }
        RecurringExpense entity = mapToEntity(request);
        RecurringExpense saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    public RecurringExpenseResponse updateRecurringExpense(Long id, RecurringExpenseRequest request) {
        RecurringExpense existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense not found"));
        // Update fields
        existing.setTitle(request.getTitle());
        existing.setAmount(request.getAmount());
        existing.setCategory(request.getCategory());
        existing.setPaymentMethod(request.getPaymentMethod());
        existing.setFrequency(parseFrequency(request.getFrequency()));
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setNextDueDate(request.getNextDueDate());
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        RecurringExpense saved = repository.save(existing);
        return mapToResponse(saved);
    }

    @Override
    public void deleteRecurringExpense(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Recurring expense not found");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringExpenseResponse getRecurringExpenseById(Long id) {
        RecurringExpense expense = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense not found"));
        return mapToResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> getAllRecurringExpenses() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> getActiveRecurringExpenses() {
        return repository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RecurringExpenseResponse pauseRecurringExpense(Long id) {
        RecurringExpense expense = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense not found"));
        expense.setIsActive(false);
        return mapToResponse(repository.save(expense));
    }

    @Override
    public RecurringExpenseResponse resumeRecurringExpense(Long id) {
        RecurringExpense expense = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring expense not found"));
        expense.setIsActive(true);
        return mapToResponse(repository.save(expense));
    }

    /**
     * Utility mapper from request DTO to entity.
     */
    private RecurringExpense mapToEntity(RecurringExpenseRequest request) {
        return RecurringExpense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory())
                .paymentMethod(request.getPaymentMethod())
                .frequency(parseFrequency(request.getFrequency()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextDueDate(request.getNextDueDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    private Frequency parseFrequency(String freq) {
        if (freq == null) {
            return Frequency.MONTHLY; // default fallback
        }
        try {
            return Frequency.valueOf(freq.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid frequency value: " + freq);
        }
    }

    /**
     * Utility mapper from entity to response DTO.
     */
    private RecurringExpenseResponse mapToResponse(RecurringExpense entity) {
        return RecurringExpenseResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .amount(entity.getAmount())
                .category(entity.getCategory())
                .paymentMethod(entity.getPaymentMethod())
                .frequency(entity.getFrequency().name())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .nextDueDate(entity.getNextDueDate())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
