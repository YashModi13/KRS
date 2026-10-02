import { Component, ElementRef, ViewChild, OnInit, AfterViewInit, HostListener } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { ToastService } from '../../services/toast.service';
import { ThemeService } from '../../services/theme.service';
import { Constants } from '../../utils/constant';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, AfterViewInit {
  username = '';
  password = '';
  isLoading = false;
  loginError: string | null = null;
  loginSuccess: boolean = false;
  showPassword = false;

  @ViewChild('usernameInput') usernameInput!: ElementRef<HTMLInputElement>;
  @ViewChild('passwordInput') passwordInput!: ElementRef<HTMLInputElement>;

  constructor(
    private authService: AuthService,
    private themeService: ThemeService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  ngAfterViewInit() {
    // Periodically check for browser autofill on page load
    const checkInterval = setInterval(() => {
      this.checkAutofill();
      if (this.username && this.password) {
        clearInterval(checkInterval);
      }
    }, 200);

    // Stop interval after 3 seconds
    setTimeout(() => clearInterval(checkInterval), 3000);
  }

  @HostListener('window:mousemove')
  @HostListener('window:keydown')
  @HostListener('window:click')
  onUserInteraction() {
    this.checkAutofill();
  }

  checkAutofill() {
    if (this.usernameInput?.nativeElement?.value && this.username !== this.usernameInput.nativeElement.value) {
      this.username = this.usernameInput.nativeElement.value;
    }
    if (this.passwordInput?.nativeElement?.value && this.password !== this.passwordInput.nativeElement.value) {
      this.password = this.passwordInput.nativeElement.value;
    }
  }

  onInput() {
    this.checkAutofill();
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.checkAutofill();
    const u = (this.username || this.usernameInput?.nativeElement?.value || '').trim();
    const p = (this.password || this.passwordInput?.nativeElement?.value || '').trim();

    if (!u || !p) {
      this.loginError = 'Please enter both username and password.';
      return;
    }

    this.isLoading = true;
    this.loginError = null;
    this.loginSuccess = false;
    
    this.authService.login({ username: u, password: p }).subscribe({
      next: data => {
        this.isLoading = false;
        this.loginSuccess = true;
        this.authService.saveToken(data.token);
        
        const username = this.authService.getUsername();
        const userTheme = data.theme || Constants.THEME.DEFAULT;
        this.themeService.applyUserTheme(userTheme, username || u);
        const role = this.authService.getRole();
        const msg = Constants.MESSAGES.SUCCESS.LOGIN_SUCCESS
          .replace('{{USERNAME}}', username)
          .replace('{{ROLE}}', role);
          
        this.toastService.success(msg);
        
        // Immediate, seamless navigation to /dashboard
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.isLoading = false;
        this.loginError = err.error?.message || 'Invalid username or password. Please try again.';
      }
    });
  }
}
