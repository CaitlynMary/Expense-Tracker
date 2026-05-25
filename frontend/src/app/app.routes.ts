import { Routes } from '@angular/router';
import { DashboardLayout } from './layout/dashboard-layout/dashboard-layout';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing').then(m => m.Landing),
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: 'signup',
    loadComponent: () => import('./features/auth/signup/signup').then(m => m.Signup)
  },
  {
    path: '',
    component: DashboardLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'expense',
        loadComponent: () => import('./features/expense/expense-list/expense-list').then(m => m.ExpenseList)
      },
      {
        path: 'category',
        loadComponent: () => import('./features/category/category-list/category-list').then(m => m.CategoryList)
      },
      {
        path: 'bill',
        loadComponent: () => import('./features/bill/bill-list/bill-list').then(m => m.BillList)
      },
      {
        path: 'budget',
        loadComponent: () => import('./features/budget/budget-list/budget-list').then(m => m.BudgetList)
      },
      {
        path: 'grocery',
        loadComponent: () => import('./features/grocery/grocery-list/grocery-list').then(m => m.GroceryList)
      },
      {
        path: 'savings',
        loadComponent: () => import('./features/savings/savings-list/savings-list').then(m => m.SavingsList)
      },
      {
        path: 'payment-methods',
        loadComponent: () => import('./features/payment-method/payment-method-list/payment-method-list').then(m => m.PaymentMethodList)
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/reports/reports/reports').then(m => m.Reports)
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/settings/settings/settings').then(m => m.Settings)
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
