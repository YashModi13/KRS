import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SystemErrorLogService, SystemErrorLog } from '../../../services/system-error-log.service';
import { Constants } from '../../../utils/constant';
import { ToastService } from '../../../services/toast.service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-system-error-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './system-error-logs.component.html',
  styleUrl: './system-error-logs.component.css'
})
export class SystemErrorLogsComponent implements OnInit {
  private errorLogService = inject(SystemErrorLogService);
  private toastService = inject(ToastService);
  readonly Constants = Constants;
  readonly Math = Math;

  errorLogs: SystemErrorLog[] = [];
  totalElements = 0;
  totalPages = 0;

  // Filters & Pagination State
  limit = Constants.PAGINATION.DEFAULT_LIMIT;
  offset = Constants.PAGINATION.DEFAULT_OFFSET;
  pageIndex = 0;
  searchQuery = '';
  selectedStatusCode: number = 0;
  isLoading = false;

  // Selected Log Drawer / Modal
  selectedLog: SystemErrorLog | null = null;

  private searchSubject = new Subject<void>();

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(400)
    ).subscribe(() => {
      this.pageIndex = 0;
      this.offset = 0;
      this.loadErrorLogs();
    });

    this.loadErrorLogs();
  }

  onSearchInput(): void {
    this.searchSubject.next();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.offset = 0;
    this.loadErrorLogs();
  }

  onLimitChange(): void {
    this.pageIndex = 0;
    this.offset = 0;
    this.loadErrorLogs();
  }

  loadErrorLogs(): void {
    this.isLoading = true;
    this.errorLogService.getErrorLogs(this.pageIndex, this.limit, this.searchQuery, this.selectedStatusCode).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.errorLogs = res.content || [];
        this.totalElements = res.totalElements || 0;
        this.totalPages = res.totalPages || 0;
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Failed to fetch system error logs', err);
        this.toastService.error('Failed to load system error logs');
      }
    });
  }

  get showingText(): string {
    if (this.totalElements === 0) {
      return 'Showing 0 of 0 error records';
    }
    const start = this.pageIndex * this.limit + 1;
    const end = Math.min((this.pageIndex + 1) * this.limit, this.totalElements);
    return `Showing ${start}–${end} of ${this.totalElements} error records`;
  }

  get isPrevDisabled(): boolean {
    return this.pageIndex <= 0;
  }

  get isNextDisabled(): boolean {
    return (this.pageIndex + 1) >= this.totalPages;
  }

  prevPage(): void {
    if (!this.isPrevDisabled) {
      this.pageIndex--;
      this.offset = this.pageIndex * this.limit;
      this.loadErrorLogs();
    }
  }

  nextPage(): void {
    if (!this.isNextDisabled) {
      this.pageIndex++;
      this.offset = this.pageIndex * this.limit;
      this.loadErrorLogs();
    }
  }

  viewLogDetails(log: SystemErrorLog): void {
    this.selectedLog = log;
  }

  closeLogDetails(): void {
    this.selectedLog = null;
  }

  clearLogs(): void {
    if (!confirm('Are you sure you want to clear all system error logs from database? This action cannot be undone.')) {
      return;
    }
    this.errorLogService.clearLogs().subscribe({
      next: () => {
        this.toastService.success('System error logs cleared successfully');
        this.selectedLog = null;
        this.loadErrorLogs();
      },
      error: (err) => {
        console.error('Failed to clear error logs', err);
        this.toastService.error('Failed to clear error logs');
      }
    });
  }

  getStatusBadgeClass(status?: number): string {
    if (!status) return 'badge-grey';
    if (status >= 500) return 'badge-amber';
    if (status >= 400) return 'badge-amber';
    return 'badge-completed';
  }
}
