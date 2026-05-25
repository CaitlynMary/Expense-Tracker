import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ApiResponse } from '../../shared/models/api.model';
import { SavingsGoal } from '../../shared/models/savings.model';

@Injectable({ providedIn: 'root' })
export class SavingsService {
  private apiService = inject(ApiService);
  private readonly path = '/savings';

  getAll(): Observable<SavingsGoal[]> {
    return this.apiService.get<ApiResponse<SavingsGoal[]>>(this.path).pipe(map(response => response.data ?? []));
  }

  create(goal: SavingsGoal): Observable<SavingsGoal> {
    return this.apiService.post<ApiResponse<SavingsGoal>>(this.path, goal).pipe(map(response => response.data));
  }

  update(id: number, goal: SavingsGoal): Observable<SavingsGoal> {
    return this.apiService.put<ApiResponse<SavingsGoal>>(`${this.path}/${id}`, goal).pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`);
  }
}
