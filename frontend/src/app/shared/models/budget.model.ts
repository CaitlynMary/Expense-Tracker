export interface Budget {
  id?: number;
  month: number;
  year: number;
  totalLimit: number;
  totalSpent?: number;
  remainingBudget?: number;
  isBudgetExceeded?: boolean;
  categoryBudgets?: CategoryBudget[];
}

export interface CategoryBudget {
  id?: number;
  category: string;
  limitAmount: number;
  spentAmount?: number;
  remainingAmount?: number;
  isExceeded?: boolean;
}
