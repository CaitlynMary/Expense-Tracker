import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryService } from '../../../core/services/category.service';
import { Category } from '../../../shared/models/category.model';
import { LucideAngularModule, Plus, Edit, Trash2 } from 'lucide-angular';
import { finalize } from 'rxjs/operators';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './category-list.html',
  styleUrls: ['./category-list.scss']
})
export class CategoryList implements OnInit {
  private categoryService = inject(CategoryService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  categories: Category[] = [];
  categoryForm: FormGroup;
  isEditing = false;
  editingId: number | null = null;
  showForm = false;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  showDeleteModal = false;
  itemToDeleteId: number | null = null;

  readonly icons = { Plus, Edit, Trash2 };

  constructor() {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      icon: [''],
      colorHex: ['#3B82F6']
    });
  }

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.categoryService.getAll()
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.categories = (data ?? [])
            .filter(category => !!category && !!category.name?.trim())
            .map(category => ({
              ...category,
              icon: category.icon?.trim() || '',
              colorHex: category.colorHex?.trim() || '#3B82F6'
            }));
          this.cdr.markForCheck();
        },
        error: error => {
          this.categories = [];
          this.errorMessage = this.getErrorMessage(error, 'Unable to load categories.');
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

  editCategory(category: Category) {
    this.isEditing = true;
    this.editingId = category.id!;
    this.showForm = true;
    this.categoryForm.patchValue({
      name: category.name,
      icon: category.icon,
      colorHex: category.colorHex || '#3B82F6'
    });
  }

  deleteCategory(id: number) {
    this.itemToDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.itemToDeleteId !== null) {
      this.categoryService.delete(this.itemToDeleteId).subscribe({
        next: () => {
          this.loadCategories();
          this.cancelDelete();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to delete category.');
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
    if (this.categoryForm.invalid) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const categoryData = this.categoryForm.value;
    const request = this.isEditing && this.editingId
      ? this.categoryService.update(this.editingId, categoryData)
      : this.categoryService.create(categoryData);

    request
      .pipe(finalize(() => {
        this.isSaving = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.loadCategories();
          this.resetForm();
          this.showForm = false;
          this.cdr.markForCheck();
        },
        error: error => {
          this.errorMessage = this.getErrorMessage(error, 'Unable to save category.');
          this.cdr.markForCheck();
        }
      });
  }

  resetForm() {
    this.isEditing = false;
    this.editingId = null;
    this.categoryForm.reset({ colorHex: '#3B82F6' });
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
