package com.familyexpensetracker.module.scheduler;

import com.familyexpensetracker.module.bill.entity.Bill;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.repository.BillRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler that marks unpaid bills as overdue when past their due date.
 * Runs daily at midnight (cron: "0 0 0 * * *").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BillReminderScheduler {

    private final BillRepository billRepository;

    /**
     * Executes every day at 00:00 server time.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateOverdueBills() {
        log.info("BillReminderScheduler started");
        LocalDate today = LocalDate.now();
        // Find bills that are not paid and due date before today.
        List<Bill> overdueBills = billRepository.findByStatusNotAndDueDateLessThanOrderByDueDateAsc(BillStatus.PAID, today);
        for (Bill bill : overdueBills) {
            try {
                bill.setStatus(BillStatus.OVERDUE);
                billRepository.save(bill);
                log.info("Bill id {} marked as OVERDUE (due {}, amount {})", bill.getId(), bill.getDueDate(), bill.getAmount());
            } catch (Exception e) {
                log.error("Failed to update bill id {}: {}", bill.getId(), e.getMessage(), e);
            }
        }
        log.info("BillReminderScheduler finished, processed {} overdue bills", overdueBills.size());
    }
}
