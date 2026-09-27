import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';

export interface PermissionDTO {
  mappingId: number;
  pageName: string;
  actionName: string;
  hasAccess: boolean;
  type: string;
}

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private krsService = inject(KrsService);

  constructor() { }

  getUserPermissions(userId: number): Observable<PermissionDTO[]> {
    return this.krsService.get<PermissionDTO[]>(`${RestUrl.PERMISSIONS}/user/${userId}`);
  }
}
