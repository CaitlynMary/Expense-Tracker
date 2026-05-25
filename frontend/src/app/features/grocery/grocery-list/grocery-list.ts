import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GroceryService } from '../../../core/services/grocery.service';
import { GroceryItem } from '../../../shared/models/grocery.model';
import { LucideAngularModule, Plus, Edit, Trash2, CheckCircle, ShoppingCart, History } from 'lucide-angular';
import { finalize } from 'rxjs/operators';

import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-grocery-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './grocery-list.html'
})
export class GroceryList implements OnInit {
  private groceryService = inject(GroceryService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  items: GroceryItem[] = [];
  historyItems: GroceryItem[] = [];
  groceryForm: FormGroup;
  showForm = false;
  isEditing = false;
  editingId: number | null = null;
  activeView: 'pending' | 'history' = 'pending';
  isLoading = false;
  isHistoryLoading = false;
  isSaving = false;
  errorMessage = '';
  showDeleteModal = false;
  itemToDeleteId: number | null = null;
  readonly icons = { Plus, Edit, Trash2, CheckCircle, ShoppingCart, History };

  constructor() {
    this.groceryForm = this.fb.group({
      name: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(0)]],
      unit: ['pcs', Validators.required],
      estimatedPrice: [0, [Validators.required, Validators.min(0)]],
      notes: ['']
    });
  }

  ngOnInit() {
    this.loadItems();
    this.loadHistory();
  }

  loadItems() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.groceryService.getAll()
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.items = data;
          this.cdr.markForCheck();
        },
        error: error => {
          this.items = [];
          this.errorMessage = this.getErrorMessage(error, 'Unable to load grocery items.');
          this.cdr.markForCheck();
        }
      });
  }

  loadHistory() {
    this.isHistoryLoading = true;
    this.cdr.markForCheck();

    this.groceryService.getHistory()
      .pipe(finalize(() => {
        this.isHistoryLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.historyItems = data;
          this.cdr.markForCheck();
        },
        error: () => {
          this.historyItems = [];
          this.cdr.markForCheck();
        }
      });
  }

  showPending() {
    this.activeView = 'pending';
    this.cdr.markForCheck();
  }

  showHistory() {
    this.activeView = 'history';
    this.loadHistory();
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) this.resetForm();
  }

  editItem(item: GroceryItem) {
    this.showForm = true;
    this.isEditing = true;
    this.editingId = item.id!;
    this.groceryForm.patchValue(item);
  }

  markPurchased(id: number) {
    this.groceryService.markPurchased(id).subscribe({
      next: purchasedItem => {
        this.items = this.items.filter(item => item.id !== id);
        this.historyItems = [purchasedItem, ...this.historyItems.filter(item => item.id !== id)];
        this.cdr.markForCheck();
        this.loadHistory();
      },
      error: error => {
        this.errorMessage = this.getErrorMessage(error, 'Unable to mark item as purchased.');
        this.cdr.markForCheck();
      }
    });
  }

  deleteItem(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.groceryService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.items = this.items.filter(item => item.id !== this.itemToDeleteId);
          this.historyItems = this.historyItems.filter(item => item.id !== this.itemToDeleteId);
          this.cancelDelete();
          this.cdr.markForCheck();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete grocery item.');
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
    if (this.groceryForm.invalid) return;
    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const action = this.isEditing && this.editingId
      ? this.groceryService.update(this.editingId, this.groceryForm.value)
      : this.groceryService.create(this.groceryForm.value);

    action
      .pipe(finalize(() => {
        this.isSaving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.loadItems();
          this.resetForm();
          this.showForm = false;
          this.activeView = 'pending';
          this.cdr.markForCheck();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to save grocery item.');
          this.cdr.markForCheck();
        }
      });
  }

  resetForm() {
    this.isEditing = false;
    this.editingId = null;
    this.groceryForm.reset({ quantity: 1, unit: 'pcs', estimatedPrice: 0 });
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
}
