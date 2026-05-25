export interface Bill {
  id?: number;
  name: string;
  amount: number;
  dueDate: string;
  paidDate?: string;
  status?: 'PENDING' | 'PAID' | 'OVERDUE';
  billType?: 'FIXED' | 'VARIABLE';
  frequency: 'ONCE' | 'DAILY' | 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'EVERY_2_MONTHS' | 'EVERY_TWO_MONTHS' | 'QUARTERLY' | 'YEARLY';
  notes?: string;
}
