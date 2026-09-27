import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';

export interface ProjectLocation {
  id?: number;
  block?: string;
  schoolId?: string;
  schoolName?: string;
  head?: string;
  newAcr?: string;
  shed?: string;
  repairing?: string;
  cwsnToilet?: string;
}

export interface Project {
  id?: number;
  workOrderNumber?: string;
  villageName?: string;
  securityDepositAmount?: number;
  retentionMoneyPerBill?: number;
  extraExcessAmount?: number;
  timeLimitExtension?: string;
  departmentName?: string;
  packageNo?: string;
  tenderId?: string;
  nameOfWork?: string;
  estimatedTenderCost?: number;
  tenderedCost?: number;
  workOrderDate?: string;
  defectsLiabilityPeriod?: string;
  locations?: ProjectLocation[];
}

export interface ProjectResponse {
  data: Project[];
  total: number;
}

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private krsService = inject(KrsService);

  getAllProjects(limit: number = 10, offset: number = 0, sortBy?: string, sortDir?: string, filters?: any): Observable<ProjectResponse> {
    let params = new HttpParams()
      .set('limit', limit.toString())
      .set('offset', offset.toString());

    if (sortBy) params = params.set('sortBy', sortBy);
    if (sortDir) params = params.set('sortDir', sortDir);

    if (filters) {
      Object.keys(filters).forEach(key => {
        if (filters[key]) {
          params = params.set(key, filters[key]);
        }
      });
    }

    return this.krsService.get<ProjectResponse>(RestUrl.PROJECTS, params);
  }

  getProjectById(id: number): Observable<Project> {
    return this.krsService.get<Project>(`${RestUrl.PROJECTS}/${id}`);
  }

  createProject(project: Project): Observable<Project> {
    return this.krsService.post<Project>(RestUrl.PROJECTS, project);
  }
}
