import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, User, Role } from '../../../services/user.service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { Constants } from '../../../utils/constant';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  totalUsers: number = 0;
  limit: number = 10;
  offset: number = 0;
  
  roles: Role[] = [];
  
  addingUser: Partial<User> | null = null;
  editingUser: User | null = null;
  changingPasswordUser: User | null = null;
  confirmPassword = '';
  showNewPassword = false;
  showConfirmPassword = false;
  isCopied = false;
  openDropdownUsername: string | null = null;
  isRoleDropdownOpen: boolean = false;
  
  isUsernameValidating = false;
  isEmailValidating = false;
  usernameError: string | null = null;
  emailError: string | null = null;
  usernameTimeout: any;
  emailTimeout: any;
  passwordValidation = {
    length: false,
    upper: false,
    lower: false,
    number: false,
    special: false,
    match: false
  };

  sortColumn: string = 'username';
  sortDirection: 'asc' | 'desc' = 'asc';

  filters: any = {
    username: '',
    email: '',
    status: '',
    roleId: ''
  };

  private filterSubject = new Subject<void>();

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.filterSubject.pipe(
      debounceTime(400)
    ).subscribe(() => {
      this.offset = 0;
      this.loadUsers();
    });

    this.loadUsers();
    this.loadRoles();
  }

  onFilterChange() {
    this.filterSubject.next();
  }

  loadUsers() {
    const apiFilters = {
      ...this.filters,
      sortBy: this.sortColumn,
      sortDir: this.sortDirection
    };
    this.userService.getUsers(this.limit, this.offset, apiFilters).subscribe(res => {
      this.users = res.data;
      this.totalUsers = res.total;
    });
  }

  toggleSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.offset = 0;
    this.loadUsers();
  }

  changePageLimit(newLimit: number) {
    this.limit = newLimit;
    this.offset = 0;
    this.loadUsers();
  }

  nextPage() {
    if (this.offset + this.limit < this.totalUsers) {
      this.offset += this.limit;
      this.loadUsers();
    }
  }

  prevPage() {
    if (this.offset >= this.limit) {
      this.offset -= this.limit;
      this.loadUsers();
    }
  }

  loadRoles() {
    this.userService.getRoles(100, 0).subscribe(res => this.roles = res.data);
  }

  editUser(user: User) {
    this.editingUser = { ...user, roles: [...user.roles] };
  }

  cancelEdit() {
    this.editingUser = null;
    this.usernameError = null;
    this.emailError = null;
    this.isRoleDropdownOpen = false;
  }

  toggleRole(role: Role) {
    if (this.editingUser) {
      this.editingUser.roles = [role];
    } else if (this.addingUser) {
      this.addingUser.roles = [role];
    }
    this.isRoleDropdownOpen = false;
  }

  saveUser() {
    if (!this.editingUser || this.usernameError || this.emailError || this.isUsernameValidating || this.isEmailValidating) return;
    
    // Check if the user being edited is the currently logged-in user
    const originalUser = this.users.find(u => u.id === this.editingUser!.id);
    const wasSelf = originalUser && originalUser.username === this.authService.getUsername();

    this.userService.updateUser(this.editingUser.id, this.editingUser, null).subscribe({
      next: () => {
        if (wasSelf) {
          // If the admin changed their own profile (which might include their username or role), 
          // the current token is instantly stale. Log them out.
          this.authService.logout();
          this.toastService.success(Constants.MESSAGES.SUCCESS.PROFILE_CHANGED_SELF);
          this.router.navigate(['/login']);
        } else {
          this.toastService.success(Constants.MESSAGES.SUCCESS.DEFAULT_UPDATE);
          this.loadUsers();
          this.editingUser = null;
          this.isRoleDropdownOpen = false;
        }
      }
    });
  }

  validateUsername() {
    clearTimeout(this.usernameTimeout);
    this.usernameError = null;
    this.isUsernameValidating = false;
    const userToValidate = this.addingUser || this.editingUser;
    
    if (!userToValidate?.username || userToValidate.username.trim() === '') {
      this.usernameError = 'Username is required';
      return;
    }
    
    this.isUsernameValidating = true;
    this.usernameTimeout = setTimeout(() => {
      this.userService.checkUsernameExists(userToValidate!.username!, userToValidate!.id).subscribe(exists => {
        this.isUsernameValidating = false;
        if (exists) {
          this.usernameError = 'Username already exists';
        }
      });
    }, 500);
  }

  validateEmail() {
    clearTimeout(this.emailTimeout);
    this.emailError = null;
    this.isEmailValidating = false;
    const userToValidate = this.addingUser || this.editingUser;
    
    if (!userToValidate?.email || userToValidate.email.trim() === '') {
      this.emailError = 'Email is required';
      return;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(userToValidate.email)) {
      this.emailError = 'Invalid email format';
      return;
    }

    this.isEmailValidating = true;
    this.emailTimeout = setTimeout(() => {
      this.userService.checkEmailExists(userToValidate!.email!, userToValidate!.id).subscribe(exists => {
        this.isEmailValidating = false;
        if (exists) {
          this.emailError = 'Email already exists';
        }
      });
    }, 500);
  }

  openChangePassword(user: User) {
    this.changingPasswordUser = { ...user, password: '' };
    this.confirmPassword = '';
    this.showNewPassword = false;
    this.showConfirmPassword = false;
    this.validatePassword();
  }

  cancelChangePassword() {
    this.changingPasswordUser = null;
    this.confirmPassword = '';
  }

  validatePassword() {
    const userToValidate = this.changingPasswordUser || this.addingUser;
    const p = userToValidate?.password || '';
    this.passwordValidation.length = p.length >= 8;
    this.passwordValidation.upper = /[A-Z]/.test(p);
    this.passwordValidation.lower = /[a-z]/.test(p);
    this.passwordValidation.number = /[0-9]/.test(p);
    this.passwordValidation.special = /[!@#$%^&*(),.?":{}|<>]/.test(p);
    this.passwordValidation.match = p === this.confirmPassword && p.length > 0;
  }

  get isPasswordValid(): boolean {
    return this.passwordValidation.length && 
           this.passwordValidation.upper && 
           this.passwordValidation.lower && 
           this.passwordValidation.number && 
           this.passwordValidation.special && 
           this.passwordValidation.match;
  }

  generatePassword() {
    const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lower = 'abcdefghijklmnopqrstuvwxyz';
    const num = '0123456789';
    const special = '!@#$%^&*()_+';
    const all = upper + lower + num + special;
    
    let pass = '';
    pass += upper[Math.floor(Math.random() * upper.length)];
    pass += lower[Math.floor(Math.random() * lower.length)];
    pass += num[Math.floor(Math.random() * num.length)];
    pass += special[Math.floor(Math.random() * special.length)];
    
    for (let i = 0; i < 6; i++) {
      pass += all[Math.floor(Math.random() * all.length)];
    }
    
    pass = pass.split('').sort(() => 0.5 - Math.random()).join('');
    
    if (this.changingPasswordUser) {
      this.changingPasswordUser.password = pass;
    } else if (this.addingUser) {
      this.addingUser.password = pass;
    }
    
    this.confirmPassword = pass;
    this.validatePassword();
  }

  copyCredentials() {
    const userToShare = this.changingPasswordUser || this.addingUser;
    if (!userToShare || !userToShare.password) return;
    
    const baseUrl = window.location.origin;
    const rolesStr = (userToShare.roles || []).map(r => r.name).join(', ') || 'User';
    const pwd = userToShare.password;
    
    const message = Constants.USER_MANAGEMENT.CREDENTIALS_SHARE_TEMPLATE
      .replace('{{APP_NAME}}', Constants.APP.NAME)
      .replace('{{URL}}', baseUrl)
      .replace('{{USERNAME}}', userToShare.username || '')
      .replace('{{EMAIL}}', userToShare.email || '')
      .replace('{{PASSWORD}}', pwd)
      .replace('{{ROLES}}', rolesStr);
    
    navigator.clipboard.writeText(message).then(() => {
      this.isCopied = true;
      setTimeout(() => this.isCopied = false, 2000);
    });
  }

  savePassword() {
    if (!this.changingPasswordUser || !this.isPasswordValid) return;
    const userId = this.changingPasswordUser.id;
    const password = this.changingPasswordUser.password!;
    const usernameChanged = this.changingPasswordUser.username;
    
    this.userService.changePassword(userId, password).subscribe({
      next: () => {
        // Check if the user changed their own password
        if (this.authService.getUsername() === usernameChanged) {
          this.authService.logout();
          this.toastService.success(Constants.MESSAGES.SUCCESS.PASSWORD_CHANGED_SELF);
          this.router.navigate(['/login']);
        } else {
          this.loadUsers();
          this.changingPasswordUser = null;
          this.confirmPassword = '';
        }
      },
      error: () => {}
    });
  }

  openAddUser() {
    this.addingUser = {
      username: '',
      email: '',
      password: '',
      roles: [],
      isActive: true
    };
    this.usernameError = null;
    this.emailError = null;
    this.confirmPassword = '';
    this.showNewPassword = false;
    this.showConfirmPassword = false;
    this.validatePassword();
  }

  cancelAdd() {
    this.addingUser = null;
    this.usernameError = null;
    this.emailError = null;
  }

  createUser() {
    if (!this.addingUser || !this.isPasswordValid || this.usernameError || this.emailError || this.isUsernameValidating || this.isEmailValidating) return;
    
    // Check if roles are selected
    if (!this.addingUser.roles || this.addingUser.roles.length === 0) {
      this.toastService.error("Please assign a role to the user");
      return;
    }

    this.userService.createUser(this.addingUser as User).subscribe({
      next: () => {
        this.filters = { username: '', email: '', status: '', roleId: '' };
        this.sortColumn = 'username';
        this.sortDirection = 'asc';
        this.offset = 0;
        this.loadUsers();
        this.addingUser = null;
      }
    });
  }

  toggleActive(user: User) {
    const updated = { ...user, isActive: !user.isActive };
    this.userService.updateUser(updated.id, updated).subscribe(() => {
      this.loadUsers();
    });
  }

  hasRole(roleId: number | undefined): boolean {
    const targetUser = this.editingUser || this.addingUser;
    if (!targetUser || roleId === undefined) return false;
    return targetUser.roles?.some(r => r.id === roleId) || false;
  }

  toggleDropdown(username: string, event: Event) {
    event.stopPropagation();
    if (this.openDropdownUsername === username) {
      this.openDropdownUsername = null;
    } else {
      this.openDropdownUsername = username;
    }
  }

  closeDropdown() {
    this.openDropdownUsername = null;
  }

  toggleRoleDropdown(event: Event) {
    event.stopPropagation();
    this.isRoleDropdownOpen = !this.isRoleDropdownOpen;
  }

  editPermissions(user: User) {
    // Placeholder for user permissions
    console.log('Edit permissions for user', user);
    // TODO: redirect or open permissions modal
  }
}

