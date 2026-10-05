import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private _toast$ = new BehaviorSubject<Toast | null>(null);
  toast$ = this._toast$.asObservable();
  private timer: any = null;

  show(message: string, type: 'success' | 'error' | 'warning' | 'info', duration = 4000) {
    if (this.timer) clearTimeout(this.timer);
    this._toast$.next({ message, type });
    this.timer = setTimeout(() => this._toast$.next(null), duration);
  }

  success(message: string) { this.show(message, 'success'); }
  error(message: string)   { this.show(message, 'error'); }
  warning(message: string) { this.show(message, 'warning'); }
  info(message: string)    { this.show(message, 'info'); }

  clear() {
    if (this.timer) clearTimeout(this.timer);
    this._toast$.next(null);
  }
}
