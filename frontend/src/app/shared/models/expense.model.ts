export interface Expense {
  id?: number;
  title: string;
  amount: number;
  category: string;
  paymentMethod: string;
  expenseDate: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}
