import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { RestUrl } from '../utils/rest-url';
import { ToastService } from './toast.service';
import { Constants } from '../utils/constant';

@Injectable({
  providedIn: 'root'
})
export class KrsService {
  private apiBaseUrl: string = RestUrl.API_BASE_URL;
  private toastService = inject(ToastService);
  private router = inject(Router);

  constructor(private http: HttpClient) {}

  get<T>(url: string, params?: any): Observable<T> {
    let httpParams = new HttpParams();
    if (params instanceof HttpParams) {
      httpParams = params;
    } else if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get<T>(`${this.apiBaseUrl}${url}`, { params: httpParams }).pipe(
      catchError(err => {
        this.toastService.error(this.extractError(err));
        return throwError(() => err);
      })
    );
  }

  post<T>(url: string, body: any, successMsg?: string | null): Observable<T> {
    return this.http.post<T>(`${this.apiBaseUrl}${url}`, body).pipe(
      tap(() => {
        if (successMsg !== null) {
          this.toastService.success(successMsg || Constants.MESSAGES.SUCCESS.DEFAULT_CREATE);
        }
      }),
      catchError(err => {
        this.toastService.error(this.extractError(err));
        return throwError(() => err);
      })
    );
  }

  put<T>(url: string, body: any, successMsg?: string | null): Observable<T> {
    return this.http.put<T>(`${this.apiBaseUrl}${url}`, body).pipe(
      tap(() => {
        if (successMsg !== null) {
          this.toastService.success(successMsg || Constants.MESSAGES.SUCCESS.DEFAULT_UPDATE);
        }
      }),
      catchError(err => {
        this.toastService.error(this.extractError(err));
        return throwError(() => err);
      })
    );
  }

  delete<T>(url: string, successMsg?: string): Observable<T> {
    return this.http.delete<T>(`${this.apiBaseUrl}${url}`).pipe(
      tap(() => this.toastService.success(successMsg || Constants.MESSAGES.SUCCESS.DEFAULT_DELETE)),
      catchError(err => {
        this.toastService.error(this.extractError(err));
        return throwError(() => err);
      })
    );
  }

  private extractError(err: any): string {
    const status = err?.status;
    if (status === 0) {
      return Constants.MESSAGES.HTTP_ERRORS.STATUS_0;
    }
    if (status === 401) {
      localStorage.clear();
      sessionStorage.clear();
      this.router.navigate(['/login']);
      return Constants.MESSAGES.HTTP_ERRORS.STATUS_401;
    }
    if (status === 403) return Constants.MESSAGES.HTTP_ERRORS.STATUS_403;
    if (status === 404) return Constants.MESSAGES.HTTP_ERRORS.STATUS_404;
    if (status === 409) return Constants.MESSAGES.HTTP_ERRORS.STATUS_409;
    if (status >= 500) return Constants.MESSAGES.HTTP_ERRORS.STATUS_500;
    return err?.error?.message || err?.message || Constants.MESSAGES.HTTP_ERRORS.DEFAULT_ERROR;
  }
}
