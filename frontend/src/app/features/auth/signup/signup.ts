import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideAngularModule, Mail, Lock, User, ArrowRight, Wallet } from 'lucide-angular';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './signup.html',
  styleUrls: ['../login/login.scss']
})
export class Signup {
  signupForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  readonly icons = { Mail, Lock, User, ArrowRight, Wallet };

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  constructor() {
    this.signupForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    console.log('[Signup] submit clicked', {
      valid: this.signupForm.valid,
      nameErrors: this.signupForm.get('name')?.errors,
      emailErrors: this.signupForm.get('email')?.errors,
      passwordErrors: this.signupForm.get('password')?.errors
    });

    this.errorMessage = '';
    this.successMessage = '';

    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      this.errorMessage = 'Please enter a valid name, email, and password with at least 6 characters.';
      this.cdr.markForCheck();
      return;
    }

    this.isLoading = true;
    this.cdr.markForCheck();

    const payload = {
      name: this.signupForm.value.name.trim(),
      email: this.signupForm.value.email.trim(),
      password: this.signupForm.value.password
    };

    console.log('[Signup] calling POST http://localhost:8080/api/auth/signup', {
      name: payload.name,
      email: payload.email
    });

    this.authService.signup(payload).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response?.message || 'Account created successfully. Opening dashboard...';
        console.log('[Signup] success response', response);
        this.cdr.markForCheck();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        console.error('[Signup] failed', err);
        this.errorMessage = this.getErrorMessage(err);
        this.cdr.markForCheck();
      }
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.signupForm.get(controlName);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  private getErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Cannot connect to backend. Make sure Spring Boot is running on http://localhost:8080.';
    }

    if (typeof err.error === 'string' && err.error.trim()) {
      return err.error;
    }

    if (err.error?.message) {
      return err.error.message;
    }

    if (err.error?.data) {
      return Object.values(err.error.data).join(' ');
    }

    return 'Registration failed. Please check the details and try again.';
  }
}
