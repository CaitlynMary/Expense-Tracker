import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SavingsService } from '../../../core/services/savings.service';
import { SavingsGoal } from '../../../shared/models/savings.model';
import { LucideAngularModule, Plus, Edit, Trash2, PiggyBank } from 'lucide-angular';
import { finalize } from 'rxjs/operators';

import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-savings-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './savings-list.html'
})
export class SavingsList implements OnInit {
  private savingsService = inject(SavingsService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);
  goals: SavingsGoal[] = [];
  goalForm: FormGroup;
  showForm = false;
  isEditing = false;
  editingId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  showDeleteModal = false;
  itemToDeleteId: number | null = null;
  readonly icons = { Plus, Edit, Trash2, PiggyBank };

  constructor() {
    this.goalForm = this.fb.group({
      name: ['', Validators.required],
      targetAmount: ['', [Validators.required, Validators.min(0.01)]],
      savedAmount: [0, [Validators.required, Validators.min(0)]],
      monthlyTarget: [0, [Validators.required, Validators.min(0)]],
      targetDate: ['', Validators.required],
      status: ['ACTIVE'],
      notes: ['']
    });
  }

  ngOnInit() { this.loadGoals(); }
  loadGoals() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.savingsService.getAll()
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.goals = data;
          this.cdr.markForCheck();
        },
        error: error => {
          console.error('Failed to fetch savings goals', error);
          this.goals = [];
          this.errorMessage = this.getErrorMessage(error, 'Unable to load savings goals.');
          this.cdr.markForCheck();
        }
      });
  }
  toggleForm() { this.showForm = !this.showForm; if (!this.showForm) this.resetForm(); }
  editGoal(goal: SavingsGoal) { this.showForm = true; this.isEditing = true; this.editingId = goal.id!; this.goalForm.patchValue(goal); }
  deleteGoal(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.savingsService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.loadGoals();
          this.cancelDelete();
        },
        error: error => {
          console.error('Failed to delete savings goal', error);
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete savings goal.');
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
    if (this.goalForm.invalid) return;
    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const action = this.isEditing && this.editingId ? this.savingsService.update(this.editingId, this.goalForm.value) : this.savingsService.create(this.goalForm.value);
    action
      .pipe(finalize(() => {
        this.isSaving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.loadGoals();
          this.resetForm();
          this.showForm = false;
          this.cdr.markForCheck();
        },
        error: error => {
          console.error('Failed to save savings goal', error);
          this.errorMessage = this.getErrorMessage(error, 'Unable to save savings goal.');
          this.cdr.markForCheck();
        }
      });
  }
  resetForm() { this.isEditing = false; this.editingId = null; this.goalForm.reset({ savedAmount: 0, monthlyTarget: 0, status: 'ACTIVE' }); }

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
