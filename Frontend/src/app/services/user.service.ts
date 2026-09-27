import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';
import { Constants } from '../utils/constant';

export interface Role {
  id?: number;
  name: string;
  description: string;
  isActive?: boolean;
}

export interface RoleResponse {
  data: Role[];
  total: number;
}

export interface User {
  id: number;
  username: string;
  email: string;
  isActive: boolean;
  roles: Role[];
  password?: string;
}

export interface UserResponse {
  data: User[];
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private krsService = inject(KrsService);

  constructor() { }

  getUsers(limit: number = Constants.PAGINATION.DEFAULT_LIMIT, offset: number = Constants.PAGINATION.DEFAULT_OFFSET, filters?: any): Observable<UserResponse> {
    let params: any = { limit, offset };
    if (filters) {
      if (filters.username) params.username = filters.username;
      if (filters.email) params.email = filters.email;
      if (filters.status !== undefined && filters.status !== '') params.status = filters.status;
      if (filters.roleId !== undefined && filters.roleId !== '') params.roleId = filters.roleId;
      if (filters.sortBy) params.sortBy = filters.sortBy;
      if (filters.sortDir) params.sortDir = filters.sortDir;
    }
    return this.krsService.get<UserResponse>(RestUrl.USERS, params);
  }

  createUser(user: User): Observable<User> {
    return this.krsService.post<User>(RestUrl.USERS, user);
  }

  updateUser(id: number, user: Partial<User>, successMsg?: string | null): Observable<User> {
    return this.krsService.put<User>(`${RestUrl.USERS}/${id}`, user, successMsg);
  }

  changePassword(id: number, password: string): Observable<User> {
    return this.krsService.put<User>(`${RestUrl.USERS}/${id}`, { password });
  }

  checkRoleNameExists(name: string, excludeId?: number): Observable<boolean> {
    const params: any = { name };
    if (excludeId !== undefined) params.excludeId = excludeId;
    return this.krsService.get<boolean>(RestUrl.USER_VALIDATION.CHECK_ROLE_NAME, params);
  }

  getRoles(limit: number = Constants.PAGINATION.DEFAULT_LIMIT, offset: number = Constants.PAGINATION.DEFAULT_OFFSET, filters?: any): Observable<RoleResponse> {
    let params: any = { limit, offset };
    if (filters) {
      if (filters.name) params.name = filters.name;
      if (filters.description) params.description = filters.description;
      if (filters.status !== undefined && filters.status !== '') params.status = filters.status;
      if (filters.sortBy) params.sortBy = filters.sortBy;
      if (filters.sortDir) params.sortDir = filters.sortDir;
    }
    return this.krsService.get<RoleResponse>(RestUrl.ROLES, params);
  }

  createRole(role: Role): Observable<Role> {
    return this.krsService.post<Role>(RestUrl.ROLES, role);
  }

  updateRole(id: number, role: Partial<Role>): Observable<Role> {
    return this.krsService.put<Role>(`${RestUrl.ROLES}/${id}`, role);
  }

  checkUsernameExists(username: string, excludeId?: number): Observable<boolean> {
    const params: any = { username };
    if (excludeId !== undefined) params.excludeId = excludeId;
    return this.krsService.get<boolean>(RestUrl.USER_VALIDATION.CHECK_USERNAME, params);
  }

  checkEmailExists(email: string, excludeId?: number): Observable<boolean> {
    const params: any = { email };
    if (excludeId !== undefined) params.excludeId = excludeId;
    return this.krsService.get<boolean>(RestUrl.USER_VALIDATION.CHECK_EMAIL, params);
  }
}

