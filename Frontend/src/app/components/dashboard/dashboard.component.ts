import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TodoService, TodoGoal } from '../../services/todo.service';

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

  constructor(private todoService: TodoService) {}

  ngOnInit() {
    this.loadTodos();
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
