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
