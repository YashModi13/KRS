import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { AuthService } from './services/auth.service';
import { NotificationService, Notification } from './services/notification.service';
import { ToastService, Toast } from './services/toast.service';
import { ThemeService } from './services/theme.service';
import { Constants } from './utils/constant';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);
  toastService = inject(ToastService);
  themeService = inject(ThemeService);
  router = inject(Router);

  toast: Toast | null = null;

  title = '';
  subTitle = '';

  isSidebarCollapsed = false;
  isNotifOpen = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  loggedInUser = '';
  loggedInRole = '';

  limit = 50;
  offset = 0;
  isLoading = false;
  hasMore = true;

  activeNotifFilter: 'all' | 'unread' | 'read' = 'all';

  get computedUnreadCount(): number {
    const unreadInList = this.notifications.filter(n => !n.isRead).length;
    return Math.max(this.unreadCount, unreadInList);
  }

  get readNotificationsCount(): number {
    return this.notifications.filter(n => n.isRead).length;
  }

  get filteredNotifications(): Notification[] {
    if (this.activeNotifFilter === 'unread') {
      return this.notifications.filter(n => !n.isRead);
    }
    if (this.activeNotifFilter === 'read') {
      return this.notifications.filter(n => n.isRead);
    }
    return this.notifications;
  }

  setNotifFilter(filter: 'all' | 'unread' | 'read') {
    this.activeNotifFilter = filter;
  }

  ngOnInit() {
    this.title = Constants.APP.NAME;
    this.subTitle = Constants.APP.SUBTITLE;

    this.toastService.toast$.subscribe(t => this.toast = t);

    // Subscribe to reactive authState$ changes
    this.authService.authState$.subscribe(isLoggedIn => {
      this.refreshUserData(isLoggedIn);
    });

    // Also refresh on navigation end
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      if (this.authService.isLoggedIn()) {
        this.refreshUserData(true);
      }
    });
  }

  refreshUserData(isLoggedIn: boolean) {
    if (isLoggedIn) {
      this.loggedInUser = this.authService.getUsername();
      this.loggedInRole = this.authService.getRole();
      
      const savedUserTheme = this.loggedInUser ? localStorage.getItem(`krs_theme_${this.loggedInUser}`) : null;
      this.themeService.applyUserTheme(savedUserTheme, this.loggedInUser);

      this.notificationService.unreadCount$.subscribe(count => this.unreadCount = count);
      this.notificationService.fetchUnreadCount();
      
      if (this.notifications.length === 0) {
        this.offset = 0;
        this.hasMore = true;
        this.loadMoreNotifications();
      }
    } else {
      this.loggedInUser = '';
      this.loggedInRole = '';
      this.notifications = [];
      this.unreadCount = 0;
      this.themeService.resetToDefaultTheme();
    }
  }

  loadMoreNotifications() {
    if (this.isLoading || !this.hasMore) return;
    this.isLoading = true;
    
    this.notificationService.fetchNotifications(this.limit, this.offset).subscribe({
      next: data => {
        if (data.length < this.limit) {
          this.hasMore = false;
        }
        this.notifications = [...this.notifications, ...data];
        this.offset += this.limit;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onScroll(event: any) {
    const element = event.target;
    if (element.scrollHeight - element.scrollTop <= element.clientHeight + 20) {
      this.loadMoreNotifications();
    }
  }

  toggleSidebar() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  toggleNotifications() {
    this.isNotifOpen = !this.isNotifOpen;
  }

  toggleNotification(notif: Notification) {
    notif.isExpanded = !notif.isExpanded;
    if (!notif.isRead) {
      notif.isRead = true;
      notif.read = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notificationService.markAsRead(notif.id).subscribe();
    }
  }

  toggleSingleRead(notif: Notification, event: Event) {
    event.stopPropagation();
    if (notif.isRead) {
      notif.isRead = false;
      notif.read = false;
      this.unreadCount++;
      this.notificationService.markAsUnread(notif.id).subscribe();
    } else {
      notif.isRead = true;
      notif.read = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notificationService.markAsRead(notif.id).subscribe();
    }
  }

  markAllAsRead() {
    this.notifications.forEach(n => {
      n.isRead = true;
      n.read = true;
    });
    this.unreadCount = 0;
    this.notificationService.markAllAsRead().subscribe();
  }

  timeAgo(dateString: string): string {
    const date = new Date(dateString);
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);
    let interval = seconds / 31536000;
    if (interval > 1) return Math.floor(interval) + " years ago";
    interval = seconds / 2592000;
    if (interval > 1) return Math.floor(interval) + " months ago";
    interval = seconds / 86400;
    if (interval > 1) return Math.floor(interval) + " days ago";
    interval = seconds / 3600;
    if (interval > 1) return Math.floor(interval) + " hours ago";
    interval = seconds / 60;
    if (interval > 1) return Math.floor(interval) + " minutes ago";
    return Math.floor(seconds) + " seconds ago";
  }

  logout() {
    const username = this.authService.getUsername();
    this.authService.logout();
    const msg = Constants.MESSAGES.SUCCESS.LOGOUT_SUCCESS.replace('{{USERNAME}}', username || 'User');
    this.toastService.success(msg);
    this.router.navigate(['/login']);
  }

  getNotifTypeClass(notif: Notification): string {
    const t = (notif.type || '').toUpperCase();
    const msg = (notif.message || '').toUpperCase();
    if (t.includes('OVERDUE') || msg.includes('OVERDUE')) return 'notif-type-overdue';
    if (t.includes('TIME_LIMIT') || msg.includes('TIME LIMIT') || msg.includes('DAYS LEFT')) return 'notif-type-timelimit';
    if (t.includes('RM') || t.includes('SD') || msg.includes('RETENTION') || msg.includes('SECURITY DEPOSIT')) return 'notif-type-finance';
    if (t.includes('APPROVAL') || msg.includes('RA BILL') || msg.includes('APPROV')) return 'notif-type-approval';
    if (t.includes('HINDRANCE') || msg.includes('HINDRANCE') || msg.includes('DISPUTE')) return 'notif-type-hindrance';
    if (t.includes('VISIT') || msg.includes('SITE VISIT') || msg.includes('INSPECT')) return 'notif-type-visit';
    if (t.includes('QUALITY') || msg.includes('MATERIAL')) return 'notif-type-quality';
    return 'notif-type-general';
  }

  getNotifIcon(notif: Notification): string {
    const cls = this.getNotifTypeClass(notif);
    switch (cls) {
      case 'notif-type-overdue': return '🚨';
      case 'notif-type-timelimit': return '⏳';
      case 'notif-type-finance': return '🔒';
      case 'notif-type-approval': return '📑';
      case 'notif-type-hindrance': return '⚠️';
      case 'notif-type-visit': return '🚗';
      case 'notif-type-quality': return '🧱';
      default: return '🔔';
    }
  }

  getNotifCategoryName(notif: Notification): string {
    const cls = this.getNotifTypeClass(notif);
    switch (cls) {
      case 'notif-type-overdue': return 'OVERDUE ALERT';
      case 'notif-type-timelimit': return 'TIME LIMIT WARNING';
      case 'notif-type-finance': return 'RM & SD RELEASE';
      case 'notif-type-approval': return 'RA BILL / APPROVAL';
      case 'notif-type-hindrance': return 'SITE HINDRANCE';
      case 'notif-type-visit': return 'SITE INSPECTION';
      case 'notif-type-quality': return 'QUALITY LAB TEST';
      default: return '';
    }
  }
}
