import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private themeSubject = new BehaviorSubject<ThemeMode>('light');
  theme$ = this.themeSubject.asObservable();

  private readonly preferencesKey = 'settings_preferences';

  constructor() {
    this.loadInitialTheme();
  }

  get currentTheme(): ThemeMode {
    return this.themeSubject.value;
  }

  setTheme(theme: ThemeMode) {
    this.themeSubject.next(theme);
    this.applyTheme(theme);
    this.saveTheme(theme);
  }

  private loadInitialTheme() {
    const rawPreferences = localStorage.getItem(this.preferencesKey);
    let theme: ThemeMode = 'light';

    if (rawPreferences) {
      try {
        const parsed = JSON.parse(rawPreferences);
        if (parsed.theme === 'dark' || parsed.theme === 'light') {
          theme = parsed.theme;
        }
      } catch (e) {
        // Ignored
      }
    }

    this.themeSubject.next(theme);
    this.applyTheme(theme);
  }

  private saveTheme(theme: ThemeMode) {
    const rawPreferences = localStorage.getItem(this.preferencesKey);
    let prefs: any = {};
    if (rawPreferences) {
      try {
        prefs = JSON.parse(rawPreferences);
      } catch (e) {
        // Ignored
      }
    }
    prefs.theme = theme;
    localStorage.setItem(this.preferencesKey, JSON.stringify(prefs));
  }

  private applyTheme(theme: ThemeMode) {
    if (theme === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }
}
