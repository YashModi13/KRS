import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';
import { Constants } from '../utils/constant';

export interface DepartmentMaster {
  id?: number;
  name: string;
  cityVillage?: string;
  state?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RefPersonMaster {
  id?: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RelatedToMaster {
  id?: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class MasterDataService {
  private krsService = inject(KrsService);

  constructor() {}

  getAllDepartmentMasters(): Observable<DepartmentMaster[]> {
    return this.krsService.get<DepartmentMaster[]>(RestUrl.DEPARTMENT_MASTERS);
  }

  getDepartmentMastersPage(page: number = Constants.PAGINATION.DEFAULT_OFFSET, size: number = Constants.PAGINATION.DEFAULT_LIMIT, search?: string): Observable<PageResponse<DepartmentMaster>> {
    const params: any = { page, size };
    if (search && search.trim()) {
      params.search = search.trim();
    }
    return this.krsService.get<PageResponse<DepartmentMaster>>(`${RestUrl.DEPARTMENT_MASTERS}/page`, params);
  }

  createDepartmentMaster(data: { name: string; cityVillage?: string; state?: string }): Observable<DepartmentMaster> {
    return this.krsService.post<DepartmentMaster>(RestUrl.DEPARTMENT_MASTERS, data);
  }

  updateDepartmentMaster(id: number, data: { name: string; cityVillage?: string; state?: string }): Observable<DepartmentMaster> {
    return this.krsService.put<DepartmentMaster>(`${RestUrl.DEPARTMENT_MASTERS}/${id}`, data);
  }

  // --- Ref Person Master ---
  getAllRefPersonMasters(): Observable<RefPersonMaster[]> {
    return this.krsService.get<RefPersonMaster[]>(RestUrl.REF_PERSON_MASTERS);
  }

  getRefPersonMastersPage(page: number = Constants.PAGINATION.DEFAULT_OFFSET, size: number = Constants.PAGINATION.DEFAULT_LIMIT, search?: string): Observable<PageResponse<RefPersonMaster>> {
    const params: any = { page, size };
    if (search && search.trim()) {
      params.search = search.trim();
    }
    return this.krsService.get<PageResponse<RefPersonMaster>>(`${RestUrl.REF_PERSON_MASTERS}/page`, params);
  }

  createRefPersonMaster(data: { name: string }): Observable<RefPersonMaster> {
    return this.krsService.post<RefPersonMaster>(RestUrl.REF_PERSON_MASTERS, data);
  }

  updateRefPersonMaster(id: number, data: { name: string }): Observable<RefPersonMaster> {
    return this.krsService.put<RefPersonMaster>(`${RestUrl.REF_PERSON_MASTERS}/${id}`, data);
  }

  // --- Related To Master ---
  getAllRelatedToMasters(): Observable<RelatedToMaster[]> {
    return this.krsService.get<RelatedToMaster[]>(RestUrl.RELATED_TO_MASTERS);
  }

  getRelatedToMastersPage(page: number = Constants.PAGINATION.DEFAULT_OFFSET, size: number = Constants.PAGINATION.DEFAULT_LIMIT, search?: string): Observable<PageResponse<RelatedToMaster>> {
    const params: any = { page, size };
    if (search && search.trim()) {
      params.search = search.trim();
    }
    return this.krsService.get<PageResponse<RelatedToMaster>>(`${RestUrl.RELATED_TO_MASTERS}/page`, params);
  }

  createRelatedToMaster(data: { name: string }): Observable<RelatedToMaster> {
    return this.krsService.post<RelatedToMaster>(RestUrl.RELATED_TO_MASTERS, data);
  }

  updateRelatedToMaster(id: number, data: { name: string }): Observable<RelatedToMaster> {
    return this.krsService.put<RelatedToMaster>(`${RestUrl.RELATED_TO_MASTERS}/${id}`, data);
  }
}
