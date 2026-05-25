import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Budget } from '../../shared/models/budget.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private apiService = inject(ApiService);
  private readonly path = '/budgets';

  getAll(): Observable<Budget[]> {
    return this.apiService.get<Budget[]>(this.path);
  }

  getById(id: number): Observable<Budget> {
    return this.apiService.get<Budget>(`${this.path}/${id}`);
  }

  getByMonthAndYear(month: number, year: number): Observable<Budget> {
    return this.apiService.get<Budget>(`${this.path}/${month}/${year}`);
  }

  create(budget: Budget): Observable<Budget> {
    return this.apiService.post<Budget>(this.path, budget);
  }

  update(id: number, budget: Budget): Observable<Budget> {
    return this.apiService.put<Budget>(`${this.path}/${id}`, budget);
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`);
  }
}
