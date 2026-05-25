package com.familyexpensetracker.module.budget.service;

import com.familyexpensetracker.exception.BadRequestException;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.budget.dto.BudgetRequest;
import com.familyexpensetracker.module.budget.dto.BudgetResponse;
import com.familyexpensetracker.module.budget.dto.CategoryBudgetDto;
import com.familyexpensetracker.module.budget.entity.Budget;
import com.familyexpensetracker.module.budget.entity.CategoryBudget;
import com.familyexpensetracker.module.budget.repository.BudgetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BudgetResponse setBudget(BudgetRequest request) {
        if (budgetRepository.existsByMonthAndYear(request.getMonth(), request.getYear())) {
            throw new BadRequestException("Budget for this month and year already exists.");
        }

        Budget budget = Budget.builder()
                .month(request.getMonth())
                .year(request.getYear())
                .totalLimit(request.getTotalLimit())
                .build();

        if (request.getCategoryBudgets() != null) {
            request.getCategoryBudgets().forEach(cbDto -> {
                CategoryBudget cb = CategoryBudget.builder()
                        .category(cbDto.getCategory())
                        .limitAmount(cbDto.getLimitAmount())
                        .build();
                budget.addCategoryBudget(cb);
            });
        }

        Budget savedBudget = budgetRepository.save(budget);
        return mapToResponse(savedBudget);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Check if changing month/year to an already existing budget
        if (!budget.getMonth().equals(request.getMonth()) || !budget.getYear().equals(request.getYear())) {
            if (budgetRepository.existsByMonthAndYear(request.getMonth(), request.getYear())) {
                throw new BadRequestException("Budget for the new month and year already exists.");
            }
        }

        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        budget.setTotalLimit(request.getTotalLimit());

        budget.getCategoryBudgets().clear();
        
        if (request.getCategoryBudgets() != null) {
            request.getCategoryBudgets().forEach(cbDto -> {
                CategoryBudget cb = CategoryBudget.builder()
                        .category(cbDto.getCategory())
                        .limitAmount(cbDto.getLimitAmount())
                        .build();
                budget.addCategoryBudget(cb);
            });
        }

        Budget updatedBudget = budgetRepository.save(budget);
        return mapToResponse(updatedBudget);
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found with id: " + id);
        }
        budgetRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        return budgetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetByMonthAndYear(Integer month, Integer year) {
        Budget budget = budgetRepository.findByMonthAndYear(month, year)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found for month: " + month + " and year: " + year));
        return mapToResponse(budget);
    }

    private BudgetResponse mapToResponse(Budget budget) {
        BigDecimal totalSpent = calculateTotalSpentForMonth(budget.getMonth(), budget.getYear());
        BigDecimal remainingBudget = budget.getTotalLimit().subtract(totalSpent);
        boolean isExceeded = remainingBudget.compareTo(BigDecimal.ZERO) < 0;

        List<BudgetResponse.CategoryBudgetResponse> categoryResponses = budget.getCategoryBudgets().stream()
                .map(cb -> {
                    BigDecimal spent = calculateTotalSpentForCategory(budget.getMonth(), budget.getYear(), cb.getCategory());
                    BigDecimal remaining = cb.getLimitAmount().subtract(spent);
                    return BudgetResponse.CategoryBudgetResponse.builder()
                            .id(cb.getId())
                            .category(cb.getCategory())
                            .limitAmount(cb.getLimitAmount())
                            .spentAmount(spent)
                            .remainingAmount(remaining)
                            .isExceeded(remaining.compareTo(BigDecimal.ZERO) < 0)
                            .build();
                }).collect(Collectors.toList());

        return BudgetResponse.builder()
                .id(budget.getId())
                .month(budget.getMonth())
                .year(budget.getYear())
                .totalLimit(budget.getTotalLimit())
                .totalSpent(totalSpent)
                .remainingBudget(remainingBudget)
                .isBudgetExceeded(isExceeded)
                .categoryBudgets(categoryResponses)
                .build();
    }

    private BigDecimal calculateTotalSpentForMonth(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String jpql = "SELECT SUM(e.amount) FROM Expense e WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate";
        TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        BigDecimal sum = query.getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal calculateTotalSpentForCategory(int month, int year, String category) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String jpql = "SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate";
        TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
        query.setParameter("category", category);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        BigDecimal sum = query.getSingleResult();
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
