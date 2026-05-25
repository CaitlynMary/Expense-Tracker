package com.familyexpensetracker.module.bill.repository;

import com.familyexpensetracker.module.bill.entity.Bill;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findAllByOrderByDueDateAsc();

    List<Bill> findByStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(BillStatus status, LocalDate date);

    List<Bill> findByStatusNotAndDueDateLessThanOrderByDueDateAsc(BillStatus status, LocalDate date);

    long countByStatus(BillStatus status);
}
