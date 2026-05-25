export interface SavingsGoal {
  id?: number;
  name: string;
  targetAmount: number;
  savedAmount: number;
  monthlyTarget: number;
  targetDate: string;
  status?: 'ACTIVE' | 'COMPLETED' | 'PAUSED' | string;
  notes?: string;
  progressPercentage?: number;
  completed?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
