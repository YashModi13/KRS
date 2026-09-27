$frontendDir = "c:\Projects\KRS\Frontend"
$srcDir = "$frontendDir\src"
$appDir = "$srcDir\app"
$servicesDir = "$appDir\services"
$componentsDir = "$appDir\components"

New-Item -ItemType Directory -Force -Path $servicesDir
New-Item -ItemType Directory -Force -Path $componentsDir
New-Item -ItemType Directory -Force -Path "$componentsDir\login"
New-Item -ItemType Directory -Force -Path "$componentsDir\dashboard"
New-Item -ItemType Directory -Force -Path "$componentsDir\admin"

@"
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import resturl from '../../assets/resturl.json';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) { }

  login(credentials: any): Observable<any> {
    return this.http.post(resturl.apiBaseUrl + resturl.loginUrl, credentials);
  }

  saveToken(token: string) {
    localStorage.setItem('auth-token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('auth-token');
  }
}
"@ | Out-File -FilePath "$servicesDir\auth.service.ts" -Encoding UTF8

@"
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.auth.getToken();
    if (token != null) {
      authReq = req.clone({ headers: req.headers.set('Authorization', 'Bearer ' + token) });
    }
    return next.handle(authReq);
  }
}
"@ | Out-File -FilePath "$servicesDir\auth.interceptor.ts" -Encoding UTF8

@"
import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="login-container">
      <div class="login-box">
        <h2>KRS Construction Login</h2>
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Username</label>
            <input type="text" [(ngModel)]="username" name="username" required>
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" [(ngModel)]="password" name="password" required>
          </div>
          <button type="submit" class="btn-primary">Login</button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f4f8; }
    .login-box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); width: 300px; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; margin-bottom: 5px; font-weight: 600; font-size: 14px; }
    .form-group input { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
    .btn-primary { width: 100%; padding: 10px; background: #2563eb; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: data => {
        this.authService.saveToken(data.token);
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        alert('Login failed');
      }
    });
  }
}
"@ | Out-File -FilePath "$componentsDir\login\login.component.ts" -Encoding UTF8

@"
import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="page-title">Dashboard</div>
    <div class="page-sub">KRS Construction Management</div>
    <div class="kpi-grid">
      <div class="kpi-card"><div class="kpi-label">Total Tenders</div><div class="kpi-value">12</div></div>
      <div class="kpi-card green"><div class="kpi-label">Active Constructions</div><div class="kpi-value">34</div></div>
    </div>
  `
})
export class DashboardComponent {}
"@ | Out-File -FilePath "$componentsDir\dashboard\dashboard.component.ts" -Encoding UTF8

@"
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
"@ | Out-File -FilePath "$appDir\app.routes.ts" -Encoding UTF8
