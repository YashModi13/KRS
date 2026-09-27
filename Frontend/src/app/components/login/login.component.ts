import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { ToastService } from '../../services/toast.service';
import { Constants } from '../../utils/constant';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  loginError: string | null = null;
  loginSuccess: boolean = false;
  showPassword = false;

  constructor(private authService: AuthService, private router: Router, private toastService: ToastService) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.loginError = null;
    this.loginSuccess = false;
    
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: data => {
        this.loginSuccess = true;
        this.authService.saveToken(data.token);
        
        const username = this.authService.getUsername();
        const role = this.authService.getRole();
        const msg = Constants.MESSAGES.SUCCESS.LOGIN_SUCCESS
          .replace('{{USERNAME}}', username)
          .replace('{{ROLE}}', role);
          
        this.toastService.success(msg);
        
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 500);
      },
      error: err => {
        this.loginError = 'Invalid username or password. Please try again.';
      }
    });
  }
}
