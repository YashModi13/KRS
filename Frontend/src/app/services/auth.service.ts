import { Injectable, inject } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { KrsService } from './krs.service';
import { ThemeService } from './theme.service';
import { RestUrl } from '../utils/rest-url';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly krsService = inject(KrsService);
  private readonly themeService = inject(ThemeService);
  private readonly authStateSubject = new BehaviorSubject<boolean>(this.isLoggedIn());
  public authState$ = this.authStateSubject.asObservable();

  login(credentials: any): Observable<any> {
    return this.krsService.post(RestUrl.AUTH.LOGIN, credentials, null);
  }

  saveToken(token: string) {
    localStorage.setItem('auth-token', token);
    this.authStateSubject.next(true);
  }

  getToken(): string | null {
    return localStorage.getItem('auth-token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getDecodedToken(): any {
    const token = this.getToken();
    if (token) {
      try {
        return JSON.parse(atob(token.split('.')[1]));
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  getUsername(): string {
    const decoded = this.getDecodedToken();
    return decoded ? decoded.sub : '';
  }

  getRole(): string {
    const decoded = this.getDecodedToken();
    if (!decoded?.role) return 'User';
    let roleStr = decoded.role.replace('ROLE_', '');
    return roleStr.charAt(0).toUpperCase() + roleStr.slice(1).toLowerCase().replace('_', ' ');
  }

  logout() {
    // Clear all Local Storage
    localStorage.clear();
    
    // Clear all Session Storage
    sessionStorage.clear();
    
    // Clear all Cookies
    const cookies = document.cookie.split("; ");
    for (let c = 0; c < cookies.length; c++) {
      const d = window.location.hostname.split(".");
      while (d.length > 0) {
        const cookieBase = encodeURIComponent(cookies[c].split(";")[0].split("=")[0]) + 
          '=; expires=Thu, 01-Jan-1970 00:00:01 GMT; domain=' + d.join('.') + ' ;path=';
        const p = location.pathname.split('/');
        document.cookie = cookieBase + '/';
        while (p.length > 0) {
          document.cookie = cookieBase + p.join('/');
          p.pop();
        }
        d.shift();
      }
    }
    this.themeService.resetToDefaultTheme();
    this.authStateSubject.next(false);
  }
}
