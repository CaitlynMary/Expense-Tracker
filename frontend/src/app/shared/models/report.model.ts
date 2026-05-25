import { Bill } from './bill.model';

export interface DashboardSummary {
  totalExpense: number;
  spentThisMonth?: number;
  totalIncome: number;
  savingsProgress: number;
  upcomingBillsCount: number;
  pendingBillsCount?: number;
  overdueBillsCount?: number;
  remainingBudget: number;
  monthlyBudget?: number;
  totalBudget?: number;
  pendingBillsAmount?: number;
  savingsSavedAmount?: number;
  savingsTargetAmount?: number;
  topSpendingCategory?: string;
  topSpendingAmount?: number;
  highestExpenseTitle?: string;
  highestExpenseAmount?: number;
  billDueSoonName?: string;
  billDueSoonDate?: string;
  
  // New fields for the redesigned reports page
  savingsAdded?: number;
  billsPaidCount?: number;
  billsPaidAmount?: number;
  pendingBills?: Bill[];
}

export interface CategoryReport {
  category: string;
  amount: number;
}

export interface MonthlyReport {
  year: number;
  month: number;
  totalExpense: number;
  totalIncome: number;
  categoryBreakdown: CategoryReport[];
}

export interface BudgetUsage {
  budgetId: number;
  category: string;
  allocatedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  usagePercentage: number;
}

export interface SavingsReport {
  totalGoalAmount: number;
  totalSavedAmount: number;
  progressPercentage: number;
}

export interface MonthlyTrend {
  monthName: string;
  month: number;
  year: number;
  totalExpense: number;
}
