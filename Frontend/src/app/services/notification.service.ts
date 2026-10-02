import { Injectable, inject } from '@angular/core';
import { Observable, BehaviorSubject, tap, map } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';

export interface Notification {
  id: number;
  message: string;
  type: string;
  isRead: boolean;
  read?: boolean;
  createdAt: string;
  isExpanded?: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private krsService = inject(KrsService);
  
  public notifications$ = new BehaviorSubject<Notification[]>([]);
  public unreadCount$ = new BehaviorSubject<number>(0);

  fetchNotifications(limit: number = 50, offset: number = 0): Observable<Notification[]> {
    return this.krsService.get<any[]>(RestUrl.NOTIFICATIONS.BASE, { limit, offset }).pipe(
      map(data => data.map(n => {
        const readStatus = n.isRead !== undefined ? n.isRead : (n.read !== undefined ? n.read : false);
        return {
          ...n,
          isRead: readStatus,
          read: readStatus
        } as Notification;
      })),
      tap(data => {
        const unread = data.filter(n => !n.isRead).length;
        this.unreadCount$.next(unread);
      })
    );
  }

  fetchUnreadCount() {
    this.krsService.get<number>(RestUrl.NOTIFICATIONS.UNREAD_COUNT).subscribe(count => {
      this.unreadCount$.next(count);
    });
  }

  markAsRead(id: number) {
    return this.krsService.put(RestUrl.NOTIFICATIONS.READ_SINGLE(id), {}, null).pipe(
      tap(() => this.fetchUnreadCount())
    );
  }

  markAsUnread(id: number) {
    return this.krsService.put(RestUrl.NOTIFICATIONS.UNREAD_SINGLE(id), {}, null).pipe(
      tap(() => this.fetchUnreadCount())
    );
  }

  markAllAsRead() {
    return this.krsService.put(RestUrl.NOTIFICATIONS.READ_ALL, {}, null).pipe(
      tap(() => this.unreadCount$.next(0))
    );
  }
}
