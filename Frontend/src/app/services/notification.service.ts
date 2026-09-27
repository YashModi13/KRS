import { Injectable, inject } from '@angular/core';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';

export interface Notification {
  id: number;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
  isExpanded?: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private krsService = inject(KrsService);
  
  public notifications$ = new BehaviorSubject<Notification[]>([]);
  public unreadCount$ = new BehaviorSubject<number>(0);

  fetchNotifications(limit: number = 50, offset: number = 0): Observable<Notification[]> {
    return this.krsService.get<Notification[]>(RestUrl.NOTIFICATIONS.BASE, { limit, offset });
  }

  fetchUnreadCount() {
    this.krsService.get<number>(RestUrl.NOTIFICATIONS.UNREAD_COUNT).subscribe(count => {
      this.unreadCount$.next(count);
    });
  }

  markAsRead(id: number) {
    return this.krsService.put(RestUrl.NOTIFICATIONS.READ_SINGLE(id), {}).pipe(
      tap(() => this.fetchUnreadCount())
    );
  }

  markAllAsRead() {
    return this.krsService.put(RestUrl.NOTIFICATIONS.READ_ALL, {}).pipe(
      tap(() => this.fetchUnreadCount())
    );
  }
}
