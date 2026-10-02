import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TodoService, TodoGoal } from '../../services/todo.service';

@Component({
  selector: 'app-daily-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './daily-tasks.component.html',
  styleUrl: './daily-tasks.component.css'
})
export class DailyTasksComponent implements OnInit {
  todos: TodoGoal[] = [];
  newTodo: TodoGoal = { taskDescription: '', priority: 'MEDIUM', targetDate: '' };
  newTodoDate: string = this.localDateStr(new Date());

  highPriorityCount = 0;
  mediumPriorityCount = 0;
  lowPriorityCount = 0;

  highPriorityTotal = 0;
  mediumPriorityTotal = 0;
  lowPriorityTotal = 0;
  
  currentDate: Date = new Date();
  
  expandedTodoId: number | null = null;
  expandedDescIds = new Set<number>();
  readonly DESC_LIMIT = 70;

  pendingDoneId: number | null = null;
  pendingDoneNote: string = '';

  readonly PRIORITY_ORDER: Record<string, number> = { HIGH: 0, MEDIUM: 1, LOW: 2 };

  constructor(private todoService: TodoService) {}

  ngOnInit() {
    this.loadTodos();
  }

  loadTodos() {
    const formattedDate = this.formatDate(this.currentDate);
    this.todoService.getActiveTodos(formattedDate).subscribe(data => {
      this.todos = data.sort((a, b) => {
        const doneA = a.isDone ? 1 : 0;
        const doneB = b.isDone ? 1 : 0;
        if (doneA !== doneB) return doneA - doneB;
        return (this.PRIORITY_ORDER[a.priority] ?? 3) - (this.PRIORITY_ORDER[b.priority] ?? 3);
      });
      this.calculateMetrics();
    });
  }

  calculateMetrics() {
    this.highPriorityCount   = this.todos.filter(t => t.priority === 'HIGH'   && !t.isDone).length;
    this.mediumPriorityCount = this.todos.filter(t => t.priority === 'MEDIUM' && !t.isDone).length;
    this.lowPriorityCount    = this.todos.filter(t => t.priority === 'LOW'    && !t.isDone).length;
    this.highPriorityTotal   = this.todos.filter(t => t.priority === 'HIGH').length;
    this.mediumPriorityTotal = this.todos.filter(t => t.priority === 'MEDIUM').length;
    this.lowPriorityTotal    = this.todos.filter(t => t.priority === 'LOW').length;
  }

  localDateStr(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  formatDate(date: Date): string {
    return this.localDateStr(date);
  }

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

  addTodo() {
    if (!this.newTodo.taskDescription) return;
    this.newTodo.targetDate = this.newTodoDate;
    this.todoService.createTodo(this.newTodo).subscribe(() => {
      this.loadTodos();
      this.newTodo = { taskDescription: '', priority: 'MEDIUM', targetDate: '' };
      this.newTodoDate = this.todayString;
    });
  }

  startMarkDone(todo: TodoGoal) {
    if (todo.isDone) return;
    this.pendingDoneId = todo.id!;
    this.pendingDoneNote = '';
  }

  cancelDone() {
    this.pendingDoneId = null;
    this.pendingDoneNote = '';
  }

  confirmDone(todo: TodoGoal) {
    if (!todo.id) return;
    this.todoService.toggleDone(todo.id, this.pendingDoneNote || undefined).subscribe(() => {
      this.pendingDoneId = null;
      this.pendingDoneNote = '';
      this.loadTodos();
    });
  }

  undoDone(todo: TodoGoal) {
    if (!todo.id || !todo.isDone) return;
    this.todoService.toggleDone(todo.id).subscribe(() => {
      this.expandedTodoId = null;
      this.loadTodos();
    });
  }

  saveDoneNote(todo: TodoGoal) {
    if (!todo.id || !todo.isDone) return;
    this.todoService.toggleDone(todo.id, todo.doneNote).subscribe(() => {
      this.loadTodos();
    });
  }

  moveNextDay(todo: TodoGoal, event: Event) {
    event.stopPropagation();
    if (!todo.id || todo.isDone) return;
    const nextDate = new Date(this.currentDate);
    nextDate.setDate(nextDate.getDate() + 1);
    this.todoService.updateTodo(todo.id, { targetDate: this.formatDate(nextDate) }).subscribe(() => {
      this.loadTodos();
    });
  }

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
