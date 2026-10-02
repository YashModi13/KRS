import { Injectable, signal, inject } from '@angular/core';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';
import { Constants } from '../utils/constant';

export type Theme = typeof Constants.THEME.LIGHT | typeof Constants.THEME.DARK;

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly krsService = inject(KrsService);
  private readonly DEFAULT_THEME: Theme = Constants.THEME.DEFAULT as Theme;
  theme = signal<Theme>(Constants.THEME.DEFAULT as Theme);
  private currentUsername: string | null = null;

  constructor() {
    this.initTheme();
  }

  private initTheme() {
    const savedTheme = localStorage.getItem('krs_theme');
    if (savedTheme === Constants.THEME.DARK || savedTheme === Constants.THEME.LIGHT) {
      this.setDomTheme(savedTheme as Theme);
    } else {
      this.resetToDefaultTheme();
    }
  }

  applyUserTheme(theme: string | null | undefined, username?: string) {
    if (username) {
      this.currentUsername = username;
    }
    const userSaved = username ? localStorage.getItem(`krs_theme_${username}`) : null;
    const themeToUse = theme || userSaved || localStorage.getItem('krs_theme');
    const selectedTheme: Theme = (themeToUse === Constants.THEME.DARK || themeToUse === Constants.THEME.LIGHT)
      ? (themeToUse as Theme)
      : this.DEFAULT_THEME;

    this.setDomTheme(selectedTheme);
    if (this.currentUsername) {
      localStorage.setItem(`krs_theme_${this.currentUsername}`, selectedTheme);
    }
  }

  toggleTheme(username?: string) {
    const nextTheme: Theme = this.theme() === Constants.THEME.LIGHT ? (Constants.THEME.DARK as Theme) : (Constants.THEME.LIGHT as Theme);
    const user = username || this.currentUsername;
    this.applyUserTheme(nextTheme, user || undefined);

    // Sync theme update to DB backend
    if (localStorage.getItem('auth-token')) {
      this.krsService.put(RestUrl.USER_THEME, { theme: nextTheme }, null).subscribe({
        error: err => console.warn('Could not sync user theme to database:', err)
      });
    }
  }

  resetToDefaultTheme() {
    this.currentUsername = null;
    this.setDomTheme(this.DEFAULT_THEME);
  }

  private setDomTheme(theme: Theme) {
    const finalTheme: Theme = (theme === Constants.THEME.DARK || theme === Constants.THEME.LIGHT) ? theme : this.DEFAULT_THEME;
    this.theme.set(finalTheme);
    document.documentElement.setAttribute('data-theme', finalTheme);
    document.body.setAttribute('data-theme', finalTheme);
    document.documentElement.dataset['theme'] = finalTheme;
    document.body.dataset['theme'] = finalTheme;
    localStorage.setItem('krs_theme', finalTheme);
  }

  isDark(): boolean {
    return this.theme() === Constants.THEME.DARK;
  }
}
