import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaymentMethodService } from '../../../core/services/payment-method.service';
import { PaymentMethod } from '../../../shared/models/payment-method.model';
import { LucideAngularModule, CreditCard, Edit, Plus, Trash2 } from 'lucide-angular';
import { finalize } from 'rxjs/operators';

import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-payment-method-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './payment-method-list.html'
})
export class PaymentMethodList implements OnInit {
  private paymentMethodService = inject(PaymentMethodService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  methods: PaymentMethod[] = [];
  methodForm: FormGroup;
  showForm = false;
  isEditing = false;
  editingId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  showDeleteModal = false;
  itemToDeleteId: number | null = null;
  readonly icons = { CreditCard, Edit, Plus, Trash2 };

  constructor() {
    this.methodForm = this.fb.group({
      methodName: ['', [Validators.required, Validators.minLength(2)]],
      description: ['']
    });
  }

  ngOnInit() { this.loadMethods(); }
  loadMethods() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.paymentMethodService.getAll()
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.methods = data;
          this.cdr.markForCheck();
        },
        error: error => {
          this.methods = [];
          this.errorMessage = this.getErrorMessage(error, 'Unable to load payment methods.');
          this.cdr.markForCheck();
        }
      });
  }
  toggleForm() { this.showForm = !this.showForm; if (!this.showForm) this.resetForm(); }
  editMethod(method: PaymentMethod) { this.showForm = true; this.isEditing = true; this.editingId = method.id!; this.methodForm.patchValue(method); }
  deleteMethod(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.paymentMethodService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.loadMethods();
          this.cancelDelete();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete payment method.');
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
    if (this.methodForm.invalid) return;
    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const action = this.isEditing && this.editingId ? this.paymentMethodService.update(this.editingId, this.methodForm.value) : this.paymentMethodService.create(this.methodForm.value);
    action
      .pipe(finalize(() => {
        this.isSaving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.loadMethods();
          this.resetForm();
          this.showForm = false;
          this.cdr.markForCheck();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to save payment method.');
          this.cdr.markForCheck();
        }
      });
  }
  resetForm() { this.isEditing = false; this.editingId = null; this.methodForm.reset(); }

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
