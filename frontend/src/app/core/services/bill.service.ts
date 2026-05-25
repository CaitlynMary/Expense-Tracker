import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Bill } from '../../shared/models/bill.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../../shared/models/api.model';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  private apiService = inject(ApiService);
  private readonly path = '/bills';

  getAll(): Observable<Bill[]> {
    return this.apiService.get<ApiResponse<Bill[]>>(this.path).pipe(map(response => response.data ?? []));
  }

  getUpcoming(): Observable<Bill[]> {
    return this.apiService.get<ApiResponse<Bill[]>>(`${this.path}/upcoming`).pipe(map(response => response.data ?? []));
  }

  getOverdue(): Observable<Bill[]> {
    return this.apiService.get<ApiResponse<Bill[]>>(`${this.path}/overdue`).pipe(map(response => response.data ?? []));
  }

  getById(id: number): Observable<Bill> {
    return this.apiService.get<ApiResponse<Bill>>(`${this.path}/${id}`).pipe(map(response => response.data));
  }

  create(bill: Bill): Observable<Bill> {
    return this.apiService.post<ApiResponse<Bill>>(this.path, bill).pipe(map(response => response.data));
  }

  update(id: number, bill: Bill): Observable<Bill> {
    return this.apiService.put<ApiResponse<Bill>>(`${this.path}/${id}`, bill).pipe(map(response => response.data));
  }

  markPaid(id: number): Observable<Bill> {
    return this.apiService.put<ApiResponse<Bill>>(`${this.path}/${id}/mark-paid`, {}).pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`);
  }
}
