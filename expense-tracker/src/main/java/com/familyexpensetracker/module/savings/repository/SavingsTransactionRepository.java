package com.familyexpensetracker.module.savings.repository;

import com.familyexpensetracker.module.savings.entity.SavingsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {
    List<SavingsTransaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    List<SavingsTransaction> findBySavingsGoalId(Long savingsGoalId);
}
