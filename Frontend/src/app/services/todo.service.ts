import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { KrsService } from './krs.service';
import { RestUrl } from '../utils/rest-url';

export interface TodoGoal {
  id?: number;
  taskDescription: string;
  priority: string;
  targetDate?: string;
  isDone?: boolean;
  doneNote?: string;
  isActive?: boolean;
  createdAt?: string;
  createdBy?: any;
  updatedAt?: string;
  updatedBy?: any;
  doneDate?: string;
  doneBy?: any;
}

@Injectable({
  providedIn: 'root'
})
export class TodoService {
  private krsService = inject(KrsService);

  constructor() { }

  getActiveTodos(date?: string): Observable<TodoGoal[]> {
    let params: any = {};
    if (date) params.date = date;
    return this.krsService.get<TodoGoal[]>(RestUrl.TODOS, params);
  }

  createTodo(todo: TodoGoal): Observable<TodoGoal> {
    return this.krsService.post<TodoGoal>(RestUrl.TODOS, todo);
  }

  updateTodo(id: number, todo: Partial<TodoGoal>): Observable<TodoGoal> {
    return this.krsService.put<TodoGoal>(`${RestUrl.TODOS}/${id}`, todo);
  }

  toggleDone(id: number, doneNote?: string): Observable<TodoGoal> {
    return this.krsService.put<TodoGoal>(`${RestUrl.TODOS}/${id}/toggle-done`, { doneNote });
  }

  deleteTodo(id: number): Observable<void> {
    return this.krsService.delete<void>(`${RestUrl.TODOS}/${id}`);
  }
}
