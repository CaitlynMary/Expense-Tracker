import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Bell, Moon, Shield, UserRound, Wallet, LogOut, Lock, Pencil } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService, ThemeMode } from '../../../core/services/theme.service';

type CurrencyCode = 'INR' | 'USD' | 'EUR';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './settings.html',
  styleUrls: ['./settings.scss']
})
export class Settings implements OnInit {
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);

  readonly icons = {
    Bell,
    Moon,
    Shield,
    UserRound,
    Wallet,
    LogOut,
    Lock,
    Pencil
  };

  billReminders = true;
  overdueNotifications = true;
  savingsReminders = true;
  currency: CurrencyCode = 'INR';
  theme: ThemeMode = 'light';
  actionMessage = '';

  private readonly preferencesKey = 'settings_preferences';

  ngOnInit() {
    this.loadPreferences();
  }

  get userName(): string {
    return this.authService.currentUser()?.name || 'User';
  }

  get userEmail(): string {
    return this.authService.currentUser()?.email || 'No email available';
  }

  savePreferences() {
    localStorage.setItem(this.preferencesKey, JSON.stringify({
      billReminders: this.billReminders,
      overdueNotifications: this.overdueNotifications,
      savingsReminders: this.savingsReminders,
      currency: this.currency
    }));
  }

  onNotificationChange() {
    this.savePreferences();
  }

  onCurrencyChange() {
    this.savePreferences();
  }

  onThemeChange() {
    this.themeService.setTheme(this.theme);
  }

  editProfile() {
    this.showActionMessage('Profile editing can be added next.');
  }

  changePassword() {
    this.showActionMessage('Password change can be connected next.');
  }

  logout() {
    this.authService.logout();
  }

  private loadPreferences() {
    // Load theme from service
    this.theme = this.themeService.currentTheme;

    const rawPreferences = localStorage.getItem(this.preferencesKey);
    if (!rawPreferences) {
      return;
    }

    try {
      const parsed = JSON.parse(rawPreferences) as {
        billReminders?: boolean;
        overdueNotifications?: boolean;
        savingsReminders?: boolean;
        currency?: CurrencyCode;
        theme?: ThemeMode;
      };

      this.billReminders = parsed.billReminders ?? true;
      this.overdueNotifications = parsed.overdueNotifications ?? true;
      this.savingsReminders = parsed.savingsReminders ?? true;
      this.currency = parsed.currency ?? 'INR';
      this.theme = this.themeService.currentTheme;
    } catch {
      this.billReminders = true;
      this.overdueNotifications = true;
      this.savingsReminders = true;
      this.currency = 'INR';
      this.theme = this.themeService.currentTheme;
    }
  }



  private showActionMessage(message: string) {
    this.actionMessage = message;
    setTimeout(() => {
      if (this.actionMessage === message) {
        this.actionMessage = '';
      }
    }, 2500);
  }
}
