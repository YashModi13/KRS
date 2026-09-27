import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { NotificationService, Notification } from './services/notification.service';
import { ToastService, Toast } from './services/toast.service';
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

  ngOnInit() {
    this.title = Constants.APP.NAME;
    this.subTitle = Constants.APP.SUBTITLE;

    this.toastService.toast$.subscribe(t => this.toast = t);

    if (this.authService.isLoggedIn()) {
      this.loggedInUser = this.authService.getUsername();
      this.loggedInRole = this.authService.getRole();
      
      this.notificationService.unreadCount$.subscribe(count => this.unreadCount = count);
      this.notificationService.fetchUnreadCount();
      
      this.loadMoreNotifications();
    }
  }

  loadMoreNotifications() {
    if (this.isLoading || !this.hasMore) return;
    this.isLoading = true;
    
    this.notificationService.fetchNotifications(this.limit, this.offset).subscribe(data => {
      if (data.length < this.limit) {
        this.hasMore = false;
      }
      this.notifications = [...this.notifications, ...data];
      this.offset += this.limit;
      this.isLoading = false;
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
      this.notificationService.markAsRead(notif.id).subscribe();
    }
  }

  markAllAsRead() {
    this.notifications.forEach(n => n.isRead = true);
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
}
