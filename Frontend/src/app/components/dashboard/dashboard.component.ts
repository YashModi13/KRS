import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TodoService, TodoGoal } from '../../services/todo.service';
import { ProjectService } from '../../services/project.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  todos: TodoGoal[] = [];
  newTodo: TodoGoal = { taskDescription: '', priority: 'MEDIUM', targetDate: '' };
  /** Date chosen in the Add form — defaults to today, no past dates allowed */
  newTodoDate: string = this.localDateStr(new Date());
  
  // SitePro V3 Dynamic Dashboard Summary Metrics
  totalProjectsCount = 0;
  totalContractValueAmount = 0;
  totalRetentionMoneyAmount = 0;
  totalSecurityDepositAmount = 0;
  dashboardProjects: any[] = [];
  isLoadingProjects = false;

  get formattedContractValue(): string {
    return '₹' + new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(this.totalContractValueAmount);
  }

  get formattedRetentionMoney(): string {
    return '₹' + new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(this.totalRetentionMoneyAmount);
  }

  get formattedSecurityDeposit(): string {
    return '₹' + new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(this.totalSecurityDepositAmount);
  }

  highPriorityCount = 0;
  mediumPriorityCount = 0;
  lowPriorityCount = 0;

  highPriorityTotal = 0;
  mediumPriorityTotal = 0;
  lowPriorityTotal = 0;
  
  currentDate: Date = new Date();
  
  /** ID of completed task whose note panel is open */
  expandedTodoId: number | null = null;

  /** IDs of tasks whose full description is expanded */
  expandedDescIds = new Set<number>();

  readonly DESC_LIMIT = 70; // chars shown when collapsed

  toggleDesc(todo: TodoGoal) {
    if (!todo.id) return;
    if (this.expandedDescIds.has(todo.id)) {
      this.expandedDescIds.delete(todo.id);
    } else {
      this.expandedDescIds.add(todo.id);
    }
  }

  isDescExpanded(todo: TodoGoal): boolean {
    return !!todo.id && this.expandedDescIds.has(todo.id);
  }

  shortDesc(text: string): string {
    return text.length > this.DESC_LIMIT ? text.slice(0, this.DESC_LIMIT).trimEnd() + '…' : text;
  }

  /** ID of pending task currently in "confirm done" mode */
  pendingDoneId: number | null = null;
  /** Temp note typed before confirming done */
  pendingDoneNote: string = '';

  maxTimeLimitDays = 90;

  constructor(
    private todoService: TodoService,
    private projectService: ProjectService
  ) {}

  ngOnInit() {
    this.loadDashboardConfig();
    this.loadTodos();
    this.loadProjectMetrics();
  }

  loadDashboardConfig() {
    this.projectService.getDashboardConfig().subscribe({
      next: (config) => {
        if (config && config.maxTimeLimitDays) {
          this.maxTimeLimitDays = config.maxTimeLimitDays;
        }
      },
      error: () => {}
    });
  }

  loadProjectMetrics() {
    this.isLoadingProjects = true;
    this.projectService.getAllProjects(100, 0).subscribe({
      next: (res) => {
        this.isLoadingProjects = false;
        if (res && res.data) {
          this.dashboardProjects = res.data;
          this.totalProjectsCount = res.total || res.data.length;
          
          let totalContract = 0;
          let totalSD = 0;
          let totalRM = 0;

          res.data.forEach(p => {
            totalContract += (p.tenderedCost || p.estimatedTenderCost || 0);
            totalSD += (p.securityDepositAmount || 0);
            
            // Calculate Retention Money from RA bills if available, or retentionMoneyPerBill
            if (p.raBills && p.raBills.length > 0) {
              p.raBills.forEach(b => {
                totalRM += (b.rm5Percent || 0);
              });
            } else if (p.retentionMoneyPerBill) {
              totalRM += p.retentionMoneyPerBill;
            }
          });

          this.totalContractValueAmount = totalContract;
          this.totalSecurityDepositAmount = totalSD;
          this.totalRetentionMoneyAmount = totalRM;
        } else {
          this.resetMetrics();
        }
      },
      error: () => {
        this.isLoadingProjects = false;
        this.resetMetrics();
      }
    });
  }

  resetMetrics() {
    this.totalProjectsCount = 0;
    this.totalContractValueAmount = 0;
    this.totalSecurityDepositAmount = 0;
    this.totalRetentionMoneyAmount = 0;
    this.dashboardProjects = [];
  }

  get timeLimitProjects(): any[] {
    if (!this.dashboardProjects || this.dashboardProjects.length === 0) return [];
    
    const today = new Date();
    const list = this.dashboardProjects.map((p) => {
      let targetEndDate: Date | null = null;
      
      if (p.completionDateExtended) {
        targetEndDate = new Date(p.completionDateExtended);
      } else if (p.timeLimitExtension) {
        targetEndDate = new Date(p.timeLimitExtension);
      } else if (p.completionDateActual) {
        targetEndDate = new Date(p.completionDateActual);
      } else if (p.workOrderDate) {
        const woDate = new Date(p.workOrderDate);
        let durationDays = 180;
        if (p.timeLimit) {
          const matchDays = p.timeLimit.match(/(\d+)\s*day/i);
          const matchMonths = p.timeLimit.match(/(\d+)\s*month/i);
          if (matchDays) durationDays = parseInt(matchDays[1], 10);
          else if (matchMonths) durationDays = parseInt(matchMonths[1], 10) * 30;
        }
        targetEndDate = new Date(woDate.getTime() + durationDays * 24 * 60 * 60 * 1000);
      } else if (p.dateOfSub) {
        const subDate = new Date(p.dateOfSub);
        targetEndDate = new Date(subDate.getTime() + 180 * 24 * 60 * 60 * 1000);
      }

      if (!targetEndDate || isNaN(targetEndDate.getTime())) {
        return null;
      }

      const diffTime = targetEndDate.getTime() - today.getTime();
      const daysLeft = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      let progressPct = 0;
      if (p.workOrderDate) {
        const woDate = new Date(p.workOrderDate);
        const totalDuration = targetEndDate.getTime() - woDate.getTime();
        const elapsed = today.getTime() - woDate.getTime();
        if (totalDuration > 0) {
          progressPct = Math.min(100, Math.max(0, Math.round((elapsed / totalDuration) * 100)));
        } else {
          progressPct = daysLeft < 0 ? 100 : 50;
        }
      } else {
        progressPct = daysLeft < 0 ? 100 : Math.max(10, Math.min(90, Math.round(100 - (daysLeft * 0.5))));
      }

      let daysColor = '#22c55e';
      let daysText = `${daysLeft}d`;
      if (daysLeft < 0) {
        daysColor = '#ef4444';
        daysText = `${Math.abs(daysLeft)}d over`;
      } else if (daysLeft <= 15) {
        daysColor = '#f97316';
      } else if (daysLeft <= 45) {
        daysColor = '#f59e0b';
      }

      return {
        ...p,
        daysLeft,
        daysText,
        daysColor,
        progressPct
      };
    }).filter((p): p is any => p !== null && p.daysLeft <= this.maxTimeLimitDays);

    return list.sort((a, b) => a.daysLeft - b.daysLeft);
  }

  get siteproAlerts(): any[] {
    if (!this.dashboardProjects || this.dashboardProjects.length === 0) return [];
    
    const alerts: any[] = [];
    const projects = this.timeLimitProjects;
    
    projects.forEach(p => {
      if (p.daysLeft < 0) {
        alerts.push({
          msg: `OVERDUE by ${Math.abs(p.daysLeft)} days`,
          project: p.nameOfWork || p.departmentName || `WO: ${p.workOrderNumber || 'N/A'}`,
          pid: p.id
        });
      } else if (p.daysLeft <= 60) {
        alerts.push({
          msg: `Site ${p.workOrderNumber ? 'WO: ' + p.workOrderNumber : 'WO-' + p.id}: ${p.daysLeft}d to deadline`,
          project: p.nameOfWork || p.departmentName || 'Project',
          pid: p.id
        });
      }
    });

    return alerts;
  }

  /** Format a Date to YYYY-MM-DD using LOCAL timezone (not UTC) */
  localDateStr(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  formatDate(date: Date): string {
    return this.localDateStr(date);
  }

  get completedTodos(): TodoGoal[] {
    return this.todos.filter(t => t.isDone);
  }

  get pendingTodos(): TodoGoal[] {
    return this.todos.filter(t => !t.isDone);
  }

  get completedTodosCount(): number {
    return this.completedTodos.length;
  }

  get totalTodosCount(): number {
    return this.todos.length;
  }

  readonly PRIORITY_ORDER: Record<string, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };

  loadTodos() {
    const formattedDate = this.formatDate(this.currentDate);
    this.todoService.getActiveTodos(formattedDate).subscribe(data => {
      this.todos = data.sort((a, b) => {
        // Primary: not-done (0) before done (1)
        const doneA = a.isDone ? 1 : 0;
        const doneB = b.isDone ? 1 : 0;
        if (doneA !== doneB) return doneA - doneB;
        // Secondary: priority within each group
        return (this.PRIORITY_ORDER[a.priority] ?? 3) - (this.PRIORITY_ORDER[b.priority] ?? 3);
      });
      this.calculateMetrics();
    });
  }
  
  prevDay() {
    this.currentDate.setDate(this.currentDate.getDate() - 1);
    this.currentDate = new Date(this.currentDate);
    this.loadTodos();
  }
  
  nextDay() {
    this.currentDate.setDate(this.currentDate.getDate() + 1);
    this.currentDate = new Date(this.currentDate);
    this.loadTodos();
  }
  
  today() {
    this.currentDate = new Date();
    this.loadTodos();
  }

  calculateMetrics() {
    this.highPriorityCount   = this.todos.filter(t => t.priority === 'HIGH'   && !t.isDone).length;
    this.mediumPriorityCount = this.todos.filter(t => t.priority === 'MEDIUM' && !t.isDone).length;
    this.lowPriorityCount    = this.todos.filter(t => t.priority === 'LOW'    && !t.isDone).length;
    this.highPriorityTotal   = this.todos.filter(t => t.priority === 'HIGH').length;
    this.mediumPriorityTotal = this.todos.filter(t => t.priority === 'MEDIUM').length;
    this.lowPriorityTotal    = this.todos.filter(t => t.priority === 'LOW').length;
  }

  /** Today's date string in local timezone — used as min constraint on date inputs */
  get todayString(): string {
    return this.localDateStr(new Date());
  }

  get currentDateString(): string {
    return this.formatDate(this.currentDate);
  }

  onDateChange(dateStr: string) {
    if (dateStr) {
      this.currentDate = new Date(dateStr);
      this.loadTodos();
    }
  }

  addTodo() {
    if (!this.newTodo.taskDescription) return;
    // Use the date chosen in the form (defaults to today)
    this.newTodo.targetDate = this.newTodoDate;
    this.todoService.createTodo(this.newTodo).subscribe(() => {
      this.loadTodos();
      this.newTodo = { taskDescription: '', priority: 'MEDIUM', targetDate: '' };
      // Reset date back to today after adding
      this.newTodoDate = this.todayString;
    });
  }

  // ── Done flow ────────────────────────────────────────────────
  /** Open the "confirm done + note" panel for a pending task */
  startMarkDone(todo: TodoGoal) {
    if (todo.isDone) return;
    this.pendingDoneId  = todo.id!;
    this.pendingDoneNote = '';
  }

  /** Cancel the pending-done panel */
  cancelDone() {
    this.pendingDoneId   = null;
    this.pendingDoneNote = '';
  }

  /** Confirm marking done — note is optional */
  confirmDone(todo: TodoGoal) {
    if (!todo.id) return;
    this.todoService.toggleDone(todo.id, this.pendingDoneNote || undefined).subscribe(() => {
      this.pendingDoneId   = null;
      this.pendingDoneNote = '';
      this.loadTodos();
    });
  }

  /** Quick-undo (un-mark done) via checkbox on a completed task */
  undoDone(todo: TodoGoal) {
    if (!todo.id || !todo.isDone) return;
    this.todoService.toggleDone(todo.id).subscribe(() => {
      this.expandedTodoId = null;
      this.loadTodos();
    });
  }

  /** Save an updated note on an already-completed task */
  saveDoneNote(todo: TodoGoal) {
    if (!todo.id || !todo.isDone) return;
    this.todoService.toggleDone(todo.id, todo.doneNote).subscribe(() => {
      this.loadTodos();
    });
  }
  // ─────────────────────────────────────────────────────────────

  moveNextDay(todo: TodoGoal, event: Event) {
    event.stopPropagation();
    if (!todo.id || todo.isDone) return;
    
    const nextDate = new Date(this.currentDate);
    nextDate.setDate(nextDate.getDate() + 1);
    
    this.todoService.updateTodo(todo.id, { targetDate: this.formatDate(nextDate) }).subscribe(() => {
      this.loadTodos();
    });
  }

  toggleDetails(todo: TodoGoal) {
    if (!todo.isDone) return;
    this.expandedTodoId = this.expandedTodoId === todo.id ? null : todo.id!;
  }

  deleteTodo(todo: TodoGoal, event: Event) {
    event.stopPropagation();
    if (todo.id) {
      this.todoService.deleteTodo(todo.id).subscribe(() => {
        this.loadTodos();
      });
    }
  }
}
