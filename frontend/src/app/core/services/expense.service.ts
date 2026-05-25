import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Expense } from '../../shared/models/expense.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PageResponse } from '../../shared/models/api.model';

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  private apiService = inject(ApiService);
  private readonly path = '/expenses';

  getAll(): Observable<Expense[]> {
    return this.apiService.get<PageResponse<Expense>>(this.path).pipe(map(response => response.content ?? []));
  }

  getById(id: number): Observable<Expense> {
    return this.apiService.get<Expense>(`${this.path}/${id}`);
  }

  create(expense: Expense): Observable<Expense> {
    return this.apiService.post<Expense>(this.path, expense);
  }

  update(id: number, expense: Expense): Observable<Expense> {
    return this.apiService.put<Expense>(`${this.path}/${id}`, expense);
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`);
  }
}
