import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BudgetService } from '../../../core/services/budget.service';
import { Budget } from '../../../shared/models/budget.model';
import { LucideAngularModule, Plus, Edit, Trash2, Wallet } from 'lucide-angular';
import { finalize } from 'rxjs/operators';

import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './budget-list.html'
})
export class BudgetList implements OnInit {
  private budgetService = inject(BudgetService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  budgets: Budget[] = [];
  budgetForm: FormGroup;
  showForm = false;
  isEditing = false;
  editingId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  showDeleteModal = false;
  itemToDeleteId: number | null = null;
  readonly icons = { Plus, Edit, Trash2, Wallet };

  constructor() {
    const today = new Date();
    this.budgetForm = this.fb.group({
      month: [today.getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]],
      year: [today.getFullYear(), [Validators.required, Validators.min(2000)]],
      totalLimit: ['', [Validators.required, Validators.min(0.01)]],
      categoryBudgets: this.fb.array([])
    });
  }

  get categoryBudgets(): FormArray {
    return this.budgetForm.get('categoryBudgets') as FormArray;
  }

  ngOnInit() {
    this.loadBudgets();
  }

  loadBudgets() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.budgetService.getAll()
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.budgets = data;
          this.cdr.markForCheck();
        },
        error: error => {
          this.budgets = [];
          this.errorMessage = this.getErrorMessage(error, 'Unable to load budgets.');
          this.cdr.markForCheck();
        }
      });
  }

  addCategoryBudget() {
    this.categoryBudgets.push(this.fb.group({
      category: ['', Validators.required],
      limitAmount: ['', [Validators.required, Validators.min(0.01)]]
    }));
  }

  removeCategoryBudget(index: number) {
    this.categoryBudgets.removeAt(index);
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) this.resetForm();
  }

  editBudget(budget: Budget) {
    this.showForm = true;
    this.isEditing = true;
    this.editingId = budget.id!;
    this.categoryBudgets.clear();
    (budget.categoryBudgets ?? []).forEach(item => {
      this.categoryBudgets.push(this.fb.group({
        category: [item.category, Validators.required],
        limitAmount: [item.limitAmount, [Validators.required, Validators.min(0.01)]]
      }));
    });
    this.budgetForm.patchValue({
      month: budget.month,
      year: budget.year,
      totalLimit: budget.totalLimit
    });
  }

  deleteBudget(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.budgetService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.loadBudgets();
          this.cancelDelete();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete budget.');
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
    if (this.budgetForm.invalid) return;
    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const action = this.isEditing && this.editingId
      ? this.budgetService.update(this.editingId, this.budgetForm.value)
      : this.budgetService.create(this.budgetForm.value);

    action
      .pipe(finalize(() => {
        this.isSaving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.loadBudgets();
          this.resetForm();
          this.showForm = false;
          this.cdr.markForCheck();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to save budget.');
          this.cdr.markForCheck();
        }
      });
  }

  resetForm() {
    const today = new Date();
    this.isEditing = false;
    this.editingId = null;
    this.categoryBudgets.clear();
    this.budgetForm.reset({ month: today.getMonth() + 1, year: today.getFullYear() });
  }

  private getErrorMessage(error: any, fallback: string): string {
    if (error?.status === 0) {
      return 'Cannot connect to backend. Please check your internet connection or try again later.';
    }
    if (error?.status === 401 || error?.status === 403) {
      return 'Your session is invalid or expired. Please log in again.';
    }
    return error?.error?.message || fallback;
  }
}
