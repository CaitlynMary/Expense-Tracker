import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { AuthResponse, LoginRequest, SignupRequest, User } from '../../shared/models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiService = inject(ApiService);
  private router = inject(Router);

  readonly currentUser = signal<User | null>(null);
  readonly isAuthenticated = signal<boolean>(false);

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly USER_NAME_KEY = 'auth_user_name';
  private readonly USER_EMAIL_KEY = 'auth_user_email';

  constructor() {
    this.checkInitialAuth();
  }

  login(credentials: LoginRequest) {
    console.log('[AuthService] POST /auth/login');
    return this.apiService.post<AuthResponse>('/auth/login', credentials).pipe(
      tap(response => this.handleAuthSuccess(response))
    );
  }

  signup(data: SignupRequest) {
    console.log('[AuthService] POST /auth/signup', { name: data.name, email: data.email });
    return this.apiService.post<AuthResponse>('/auth/signup', data).pipe(
      tap(response => this.handleAuthSuccess(response))
    );
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.USER_NAME_KEY);
    localStorage.removeItem(this.USER_EMAIL_KEY);
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private checkInitialAuth() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const user = this.readStoredUser();

    if (token) {
      this.isAuthenticated.set(true);
      if (user?.name && user?.email) {
        this.currentUser.set(user);
      } else {
        this.currentUser.set(null);
        this.loadCurrentUserProfile();
      }
    } else {
      this.isAuthenticated.set(false);
      this.currentUser.set(null);
    }
  }

  private handleAuthSuccess(response: AuthResponse) {
    console.log('[AuthService] auth success', response);
    if (!response?.token) {
      return;
    }

    const user: User = response.user ?? {
      id: response.id,
      name: response.name,
      email: response.email
    };

    localStorage.setItem(this.TOKEN_KEY, response.token);
    this.persistUser(user);
    this.router.navigate(['/dashboard']);
  }

  private readStoredUser(): User | null {
    const rawUser = localStorage.getItem(this.USER_KEY);
    if (rawUser) {
      try {
        return JSON.parse(rawUser) as User;
      } catch (error) {
        console.error('[AuthService] Failed to parse stored user.', error);
        localStorage.removeItem(this.USER_KEY);
      }
    }

    const name = localStorage.getItem(this.USER_NAME_KEY);
    const email = localStorage.getItem(this.USER_EMAIL_KEY);

    if (name && email) {
      return {
        id: 0,
        name,
        email
      };
    }

    return null;
  }

  private loadCurrentUserProfile() {
    this.apiService.get<AuthResponse>('/auth/me').subscribe({
      next: (response) => {
        const user: User = {
          id: response.id,
          name: response.name,
          email: response.email
        };
        this.persistUser(user);
      },
      error: (error) => {
        console.error('[AuthService] Failed to restore current user profile.', error);
        this.logout();
      }
    });
  }

  private persistUser(user: User) {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    localStorage.setItem(this.USER_NAME_KEY, user.name);
    localStorage.setItem(this.USER_EMAIL_KEY, user.email);
    this.currentUser.set(user);
    this.isAuthenticated.set(true);
  }
}
