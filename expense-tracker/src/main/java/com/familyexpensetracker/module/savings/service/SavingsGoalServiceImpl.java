package com.familyexpensetracker.module.savings.service;

import com.familyexpensetracker.common.mapper.SavingsGoalMapper;
import com.familyexpensetracker.exception.DuplicateResourceException;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.savings.dto.SavingsGoalRequest;
import com.familyexpensetracker.module.savings.dto.SavingsGoalResponse;
import com.familyexpensetracker.module.savings.entity.SavingsGoal;
import com.familyexpensetracker.module.savings.entity.SavingsGoalStatus;
import com.familyexpensetracker.module.savings.entity.SavingsTransaction;
import com.familyexpensetracker.module.savings.repository.SavingsGoalRepository;
import com.familyexpensetracker.module.savings.repository.SavingsTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    @Override
    @Transactional
    public SavingsGoalResponse createSavingsGoal(SavingsGoalRequest request) {
        // check duplicate name
        if (savingsGoalRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("SavingsGoal", "name", request.getName());
        }
        SavingsGoal goal = savingsGoalMapper.toEntity(request);
        // default status if not set
        if (goal.getStatus() == null) {
            goal.setStatus(SavingsGoalStatus.IN_PROGRESS);
        }
        SavingsGoal saved = savingsGoalRepository.save(goal);
        
        if (saved.getSavedAmount() != null && saved.getSavedAmount().compareTo(BigDecimal.ZERO) > 0) {
            savingsTransactionRepository.save(SavingsTransaction.builder()
                    .savingsGoalId(saved.getId())
                    .amount(saved.getSavedAmount())
                    .transactionDate(LocalDate.now())
                    .build());
        }
        
        return savingsGoalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateSavingsGoal(Long id, SavingsGoalRequest request) {
        SavingsGoal existing = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        // duplicate name check excluding current id
        if (savingsGoalRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("SavingsGoal", "name", request.getName());
        }
        BigDecimal oldSaved = existing.getSavedAmount() != null ? existing.getSavedAmount() : BigDecimal.ZERO;
        savingsGoalMapper.updateEntity(existing, request);
        BigDecimal newSaved = existing.getSavedAmount() != null ? existing.getSavedAmount() : BigDecimal.ZERO;
        
        // recalculate status if savedAmount reached target
        if (existing.getSavedAmount().compareTo(existing.getTargetAmount()) >= 0) {
            existing.setStatus(SavingsGoalStatus.COMPLETED);
        } else if (existing.getStatus() == SavingsGoalStatus.COMPLETED) {
            existing.setStatus(SavingsGoalStatus.IN_PROGRESS);
        }
        SavingsGoal saved = savingsGoalRepository.save(existing);
        
        if (newSaved.compareTo(oldSaved) > 0) {
            savingsTransactionRepository.save(SavingsTransaction.builder()
                    .savingsGoalId(saved.getId())
                    .amount(newSaved.subtract(oldSaved))
                    .transactionDate(LocalDate.now())
                    .build());
        }
        
        return savingsGoalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSavingsGoal(Long id) {
        SavingsGoal existing = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        
        // Delete associated transactions
        List<SavingsTransaction> txs = savingsTransactionRepository.findBySavingsGoalId(id);
        if (txs != null && !txs.isEmpty()) {
            savingsTransactionRepository.deleteAll(txs);
        }
        
        savingsGoalRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalResponse getSavingsGoalById(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        return savingsGoalMapper.toResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getAllSavingsGoals() {
        return savingsGoalRepository.findAll()
                .stream()
                .map(savingsGoalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SavingsGoalResponse addSavingsAmount(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        BigDecimal newSaved = goal.getSavedAmount().add(amount);
        goal.setSavedAmount(newSaved);
        // check completion
        if (newSaved.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoalStatus.COMPLETED);
        } else if (goal.getStatus() == SavingsGoalStatus.COMPLETED) {
            goal.setStatus(SavingsGoalStatus.IN_PROGRESS);
        }
        SavingsGoal saved = savingsGoalRepository.save(goal);
        
        savingsTransactionRepository.save(SavingsTransaction.builder()
                .savingsGoalId(saved.getId())
                .amount(amount)
                .transactionDate(LocalDate.now())
                .build());
                
        return savingsGoalMapper.toResponse(saved);
    }
}
