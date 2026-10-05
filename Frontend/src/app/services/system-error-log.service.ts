import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestUrl } from '../utils/rest-url';

export interface SystemErrorLog {
  id?: number;
  timestamp?: string;
  errorType?: string;
  message?: string;
  stackTrace?: string;
  endpoint?: string;
  httpMethod?: string;
  userName?: string;
  statusCode?: number;
  clientIp?: string;
  createdAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class SystemErrorLogService {
  private http = inject(HttpClient);

  getErrorLogs(page: number = 0, size: number = 10, search?: string, statusCode?: number): Observable<PageResponse<SystemErrorLog>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }

    if (statusCode !== undefined && statusCode !== null && statusCode !== 0) {
      params = params.set('statusCode', statusCode.toString());
    }

    return this.http.get<PageResponse<SystemErrorLog>>(RestUrl.SYSTEM_ERROR_LOGS, { params });
  }

  clearLogs(): Observable<void> {
    return this.http.delete<void>(RestUrl.SYSTEM_ERROR_LOGS);
  }
}
