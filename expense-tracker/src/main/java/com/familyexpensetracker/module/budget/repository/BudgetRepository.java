package com.familyexpensetracker.module.budget.repository;

import com.familyexpensetracker.module.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMonthAndYear(Integer month, Integer year);
    boolean existsByMonthAndYear(Integer month, Integer year);
}
