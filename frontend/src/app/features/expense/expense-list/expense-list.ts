import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { GlobalSearchService } from '../../../core/services/global-search.service';
import { ExpenseService } from '../../../core/services/expense.service';
import { CategoryService } from '../../../core/services/category.service';
import { PaymentMethodService } from '../../../core/services/payment-method.service';
import { Expense } from '../../../shared/models/expense.model';
import { Category } from '../../../shared/models/category.model';
import { PaymentMethod } from '../../../shared/models/payment-method.model';
import { LucideAngularModule, Plus, Edit, Trash2, Receipt, Utensils, Car, ShoppingCart, Film, HeartPulse, Book, ShoppingBag, Home, Trophy, Wallet } from 'lucide-angular';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './expense-list.html',
  styleUrls: ['./expense-list.scss']
})
export class ExpenseList implements OnInit {
  private expenseService = inject(ExpenseService);
  private route = inject(ActivatedRoute);
  private globalSearchService = inject(GlobalSearchService);
  private categoryService = inject(CategoryService);
  private paymentMethodService = inject(PaymentMethodService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  expenses: Expense[] = [];
  categories: Category[] = [];
  paymentMethods: PaymentMethod[] = [];
  expenseForm: FormGroup;
  isEditing = false;
  editingId: number | null = null;
  showForm = false;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  private submitAttemptCount = 0;
  highlightedExpenseId: number | null = null;
  
  showDeleteModal = false;
  itemToDeleteId: number | null = null;

  readonly icons = { Plus, Edit, Trash2, Receipt, Utensils, Car, ShoppingCart, Film, HeartPulse, Book, ShoppingBag, Home, Trophy, Wallet };

  constructor() {
    this.expenseForm = this.fb.group({
      title: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      expenseDate: ['', Validators.required],
      category: ['', Validators.required],
      paymentMethod: ['', Validators.required],
      notes: ['']
    });
  }

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => {
      const highlightType = params.get('highlightType');
      const highlightId = params.get('highlightId');
      this.highlightedExpenseId = highlightType === 'expense' && highlightId ? Number(highlightId) : null;
      this.scrollToHighlightedExpense();
      this.cdr.markForCheck();
    });

    this.loadExpenses();
    this.loadCategories();
    this.loadPaymentMethods();
  }

  loadExpenses() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.expenseService.getAll().subscribe({
      next: data => {
        this.expenses = data;
        this.isLoading = false;
        this.scrollToHighlightedExpense();
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.errorMessage = this.getErrorMessage(error, 'Unable to load expenses.');
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadCategories() {
    this.categoryService.getAll().subscribe({
      next: data => {
        this.categories = data;
        this.cdr.markForCheck();
      },
      error: () => {
        // Categories failed to load — form dropdowns will be empty
        this.categories = [];
        this.cdr.markForCheck();
      }
    });
  }

  loadPaymentMethods() {
    this.paymentMethodService.getAll().subscribe({
      next: data => {
        this.paymentMethods = data;
        this.cdr.markForCheck();
      },
      error: () => {
        // Payment methods failed to load — form dropdowns will be empty
        this.paymentMethods = [];
        this.cdr.markForCheck();
      }
    });
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.resetForm();
    }
  }

  editExpense(expense: Expense) {
    this.isEditing = true;
    this.editingId = expense.id!;
    this.showForm = true;
    this.expenseForm.patchValue({
      title: expense.title,
      amount: expense.amount,
      expenseDate: expense.expenseDate,
      category: expense.category,
      paymentMethod: expense.paymentMethod,
      notes: expense.notes
    });
  }

  deleteExpense(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.expenseService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.globalSearchService.refresh();
          this.loadExpenses();
          this.cancelDelete();
        },
        error: (error) => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete expense.');
          this.cancelDelete();
          this.cdr.markForCheck();
        }
      });
    }
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.itemToDeleteId = null;
  }

  onSubmit() {
    if (this.isSaving) {
      console.log('[ExpenseList] Ignoring duplicate submit while request is in progress.');
      return;
    }

    if (this.expenseForm.invalid) {
      return;
    }

    this.submitAttemptCount += 1;
    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const request = this.expenseForm.value;
    console.log('[ExpenseList] Submit triggered.', {
      submitAttemptCount: this.submitAttemptCount,
      mode: this.isEditing ? 'update' : 'create',
      request
    });

    const action = this.isEditing && this.editingId
      ? this.expenseService.update(this.editingId, request)
      : this.expenseService.create(request);

    action.subscribe({
      next: () => {
        console.log('[ExpenseList] Save request completed successfully.', {
          submitAttemptCount: this.submitAttemptCount
        });
        this.isSaving = false;
        this.globalSearchService.refresh();
        this.loadExpenses();
        this.resetForm();
        this.showForm = false;
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('[ExpenseList] Save request failed.', {
          submitAttemptCount: this.submitAttemptCount,
          error
        });
        this.isSaving = false;
        this.errorMessage = this.getErrorMessage(error, 'Unable to save expense.');
        this.cdr.markForCheck();
      }
    });
  }

  resetForm() {
    this.isEditing = false;
    this.editingId = null;
    this.expenseForm.reset();
  }

  private getErrorMessage(error: any, fallback: string): string {
    if (error?.status === 0) {
      return 'Cannot connect to backend. Make sure Spring Boot is running on http://localhost:8080.';
    }
    if (error?.status === 401 || error?.status === 403) {
      return 'Your session is invalid or expired. Please log in again.';
    }
    return error?.error?.message || fallback;
  }

  private scrollToHighlightedExpense() {
    if (!this.highlightedExpenseId || this.expenses.length === 0) {
      return;
    }

    setTimeout(() => {
      const target = document.getElementById(`expense-item-${this.highlightedExpenseId}`);
      target?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 0);
  }

  getCategoryIcon(categoryName: string): any {
    const lowerCategory = categoryName?.toLowerCase() || '';
    if (lowerCategory.includes('food') || lowerCategory.includes('dining')) return this.icons.Utensils;
    if (lowerCategory.includes('transport') || lowerCategory.includes('car') || lowerCategory.includes('bike')) return this.icons.Car;
    if (lowerCategory.includes('groceries')) return this.icons.ShoppingCart;
    if (lowerCategory.includes('entertainment')) return this.icons.Film;
    if (lowerCategory.includes('bill') || lowerCategory.includes('utilities')) return this.icons.Receipt;
    if (lowerCategory.includes('health') || lowerCategory.includes('medical')) return this.icons.HeartPulse;
    if (lowerCategory.includes('education')) return this.icons.Book;
    if (lowerCategory.includes('shopping')) return this.icons.ShoppingBag;
    if (lowerCategory.includes('rent') || lowerCategory.includes('housing') || lowerCategory.includes('home')) return this.icons.Home;
    if (lowerCategory.includes('sports')) return this.icons.Trophy;
    return this.icons.Wallet;
  }
}
