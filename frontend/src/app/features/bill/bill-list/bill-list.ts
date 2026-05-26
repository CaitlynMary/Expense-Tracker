import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BillService } from '../../../core/services/bill.service';
import { GlobalSearchService } from '../../../core/services/global-search.service';
import { Bill } from '../../../shared/models/bill.model';
import { LucideAngularModule, Plus, Edit, Trash2, Calendar, CheckCircle } from 'lucide-angular';
import { finalize } from 'rxjs/operators';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-bill-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './bill-list.html',
  styleUrls: ['./bill-list.scss']
})
export class BillList implements OnInit {
  private billService = inject(BillService);
  private route = inject(ActivatedRoute);
  private globalSearchService = inject(GlobalSearchService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  bills: Bill[] = [];
  billForm: FormGroup;
  isEditing = false;
  editingId: number | null = null;
  showForm = false;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  highlightedBillId: number | null = null;
  
  showDeleteModal = false;
  itemToDeleteId: number | null = null;
  readonly billTypes = [
    { value: 'FIXED', label: 'Fixed' },
    { value: 'VARIABLE', label: 'Variable' }
  ];
  readonly frequencies = [
    { value: 'ONCE', label: 'One Time' },
    { value: 'WEEKLY', label: 'Weekly' },
    { value: 'MONTHLY', label: 'Monthly' },
    { value: 'EVERY_TWO_MONTHS', label: 'Every 2 Months' },
    { value: 'QUARTERLY', label: 'Quarterly' },
    { value: 'YEARLY', label: 'Yearly' }
  ];

  readonly icons = { Plus, Edit, Trash2, Calendar, CheckCircle };

  constructor() {
    this.billForm = this.fb.group({
      name: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      dueDate: ['', Validators.required],
      billType: ['FIXED', Validators.required],
      frequency: ['MONTHLY', Validators.required],
      notes: ['']
    });
  }

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => {
      const highlightType = params.get('highlightType');
      const highlightId = params.get('highlightId');
      this.highlightedBillId = highlightType === 'bill' && highlightId ? Number(highlightId) : null;
      this.scrollToHighlightedBill();
      this.cdr.markForCheck();
    });

    this.loadBills();
  }

  loadBills() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.billService.getAll()
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.bills = data;
          this.scrollToHighlightedBill();
          this.cdr.markForCheck();
        },
        error: error => {
          console.error('Failed to fetch bills', error);
          this.bills = [];
          this.errorMessage = this.getErrorMessage(error, 'Unable to load bills.');
          this.showToast(this.errorMessage, 'error');
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

  editBill(bill: Bill) {
    this.isEditing = true;
    this.editingId = bill.id!;
    this.showForm = true;
    
    const dateStr = new Date(bill.dueDate).toISOString().split('T')[0];
    
    this.billForm.patchValue({
      name: bill.name,
      amount: bill.amount,
      dueDate: dateStr,
      billType: bill.billType || 'FIXED',
      frequency: bill.frequency,
      notes: bill.notes
    });
  }

  deleteBill(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.billService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.globalSearchService.refresh();
          this.loadBills();
          this.showToast('Bill deleted successfully.', 'success');
          this.cancelDelete();
        },
        error: error => {
          console.error('Failed to delete bill', error);
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete bill.');
          this.showToast(this.errorMessage, 'error');
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

  markAsPaid(id: number) {
    this.billService.markPaid(id).subscribe({
        next: updatedBill => {
          this.globalSearchService.refresh();
          this.loadBills();
          const message = updatedBill.frequency === 'ONCE'
            ? 'One-time bill archived as paid.'
            : `Bill paid. Next due date: ${this.formatDate(updatedBill.dueDate)}.`;
          this.showToast(message, 'success');
        },
      error: error => {
        console.error('Failed to mark bill as paid', error);
        this.errorMessage = this.getErrorMessage(error, 'Unable to mark bill as paid.');
        this.showToast(this.errorMessage, 'error');
        this.cdr.markForCheck();
      }
    });
  }

  onSubmit() {
    if (this.billForm.invalid) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const billData = this.billForm.value;
    const action = this.isEditing && this.editingId
      ? this.billService.update(this.editingId, billData)
      : this.billService.create(billData);

    action
      .pipe(finalize(() => {
        this.isSaving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          const successMessage = this.isEditing ? 'Bill updated successfully.' : 'Bill saved successfully.';
          this.globalSearchService.refresh();
          this.loadBills();
          this.resetForm();
          this.showForm = false;
          this.showToast(successMessage, 'success');
          this.cdr.markForCheck();
        },
        error: error => {
          console.error('Failed to save bill', error);
          this.errorMessage = this.getErrorMessage(error, 'Unable to save bill.');
          this.showToast(this.errorMessage, 'error');
          this.cdr.markForCheck();
        }
      });
  }

  resetForm() {
    this.isEditing = false;
    this.editingId = null;
    this.billForm.reset({ billType: 'FIXED', frequency: 'MONTHLY' });
  }

  getFrequencyLabel(frequency: Bill['frequency']): string {
    return this.frequencies.find(item => item.value === frequency)?.label || frequency;
  }

  getBillTypeLabel(billType: Bill['billType']): string {
    return billType === 'VARIABLE' ? 'Variable Bill' : 'Fixed Bill';
  }

  private formatDate(date: string): string {
    return new Date(date).toLocaleDateString();
  }

  private showToast(message: string, type: 'success' | 'error') {
    this.toastMessage = message;
    this.toastType = type;
    this.cdr.markForCheck();

    setTimeout(() => {
      if (this.toastMessage === message) {
        this.toastMessage = '';
        this.cdr.markForCheck();
      }
    }, 3000);
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

  private scrollToHighlightedBill() {
    if (!this.highlightedBillId || this.bills.length === 0) {
      return;
    }

    setTimeout(() => {
      const target = document.getElementById(`bill-item-${this.highlightedBillId}`);
      target?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 0);
  }
}
