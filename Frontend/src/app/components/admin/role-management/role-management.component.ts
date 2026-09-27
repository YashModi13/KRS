import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, Role } from '../../../services/user.service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-management.component.html',
  styleUrls: ['./role-management.component.css']
})
export class RoleManagementComponent implements OnInit {
  roles: Role[] = [];
  totalRoles: number = 0;
  limit: number = 10;
  offset: number = 0;
  
  editingRole: Role | null = null;
  isNew: boolean = false;
  openDropdownRoleName: string | null = null;

  sortColumn: string = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  filters: any = {
    name: '',
    description: '',
    status: ''
  };
  filterSubject: Subject<void> = new Subject<void>();

  isNameValidating = false;
  nameError: string | null = null;
  nameTimeout: any;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.filterSubject.pipe(
      debounceTime(400)
    ).subscribe(() => {
      this.offset = 0;
      this.loadRoles();
    });

    this.loadRoles();
  }

  onFilterChange() {
    this.filterSubject.next();
  }

  loadRoles() {
    const apiFilters = {
      ...this.filters,
      sortBy: this.sortColumn,
      sortDir: this.sortDirection
    };
    this.userService.getRoles(this.limit, this.offset, apiFilters).subscribe(res => {
      this.roles = res.data;
      this.totalRoles = res.total;
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
    this.loadRoles();
  }

  changePageLimit(newLimit: number) {
    this.limit = newLimit;
    this.offset = 0;
    this.loadRoles();
  }

  nextPage() {
    if (this.offset + this.limit < this.totalRoles) {
      this.offset += this.limit;
      this.loadRoles();
    }
  }

  prevPage() {
    if (this.offset >= this.limit) {
      this.offset -= this.limit;
      this.loadRoles();
    }
  }

  createRole() {
    this.isNew = true;
    this.editingRole = { name: '', description: '', isActive: true };
    this.nameError = null;
    this.isNameValidating = false;
  }

  editRole(role: Role) {
    this.isNew = false;
    this.editingRole = { ...role };
    this.nameError = null;
    this.isNameValidating = false;
  }

  cancelEdit() {
    this.editingRole = null;
    this.nameError = null;
  }

  validateName() {
    clearTimeout(this.nameTimeout);
    this.nameError = null;
    this.isNameValidating = false;
    
    if (!this.editingRole?.name || this.editingRole.name.trim() === '') {
      this.nameError = 'Role name is required';
      return;
    }
    
    this.isNameValidating = true;
    this.nameTimeout = setTimeout(() => {
      this.userService.checkRoleNameExists(this.editingRole!.name, this.editingRole!.id).subscribe(exists => {
        this.isNameValidating = false;
        if (exists) {
          this.nameError = 'Role name already exists';
        }
      });
    }, 500);
  }

  saveRole() {
    if (!this.editingRole || this.nameError || this.isNameValidating) return;
    
    if (this.isNew) {
      this.userService.createRole(this.editingRole).subscribe(() => {
        this.filters = { name: '', description: '', status: '' };
        this.sortColumn = 'name';
        this.sortDirection = 'asc';
        this.offset = 0;
        this.loadRoles();
        this.editingRole = null;
      });
    } else if (this.editingRole.id) {
      this.userService.updateRole(this.editingRole.id, this.editingRole).subscribe(() => {
        this.loadRoles();
        this.editingRole = null;
      });
    }
  }

  toggleDropdown(roleName: string, event: Event) {
    event.stopPropagation();
    if (this.openDropdownRoleName === roleName) {
      this.openDropdownRoleName = null;
    } else {
      this.openDropdownRoleName = roleName;
    }
  }

  closeDropdown() {
    this.openDropdownRoleName = null;
  }

  editPermissions(role: Role) {
    console.log('Edit permissions for role', role);
  }
  
  toggleActive(role: Role) {
    if (!role.id) return;
    const newStatus = !role.isActive;
    this.userService.updateRole(role.id, { isActive: newStatus }).subscribe(() => {
      role.isActive = newStatus;
    });
  }
}

