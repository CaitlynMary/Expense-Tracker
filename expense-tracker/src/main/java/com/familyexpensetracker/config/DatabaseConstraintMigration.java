package com.familyexpensetracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseConstraintMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        updateBillsFrequencyConstraint();
    }

    private void updateBillsFrequencyConstraint() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = 'bills'
                    ) THEN
                        ALTER TABLE bills DROP CONSTRAINT IF EXISTS bills_frequency_check;
                        ALTER TABLE bills ADD CONSTRAINT bills_frequency_check
                            CHECK (frequency IN (
                                'ONCE',
                                'DAILY',
                                'WEEKLY',
                                'BIWEEKLY',
                                'MONTHLY',
                                'EVERY_2_MONTHS',
                                'EVERY_TWO_MONTHS',
                                'QUARTERLY',
                                'YEARLY'
                            ));
                    END IF;
                END $$;
                """);
    }
}
