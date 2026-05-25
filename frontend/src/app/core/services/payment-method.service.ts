import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ApiResponse } from '../../shared/models/api.model';
import { PaymentMethod } from '../../shared/models/payment-method.model';

@Injectable({ providedIn: 'root' })
export class PaymentMethodService {
  private apiService = inject(ApiService);
  private readonly path = '/payment-methods';

  getAll(): Observable<PaymentMethod[]> {
    return this.apiService.get<ApiResponse<PaymentMethod[]>>(this.path).pipe(map(response => response.data ?? []));
  }

  create(method: PaymentMethod): Observable<PaymentMethod> {
    return this.apiService.post<ApiResponse<PaymentMethod>>(this.path, method).pipe(map(response => response.data));
  }

  update(id: number, method: PaymentMethod): Observable<PaymentMethod> {
    return this.apiService.put<ApiResponse<PaymentMethod>>(`${this.path}/${id}`, method).pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`);
  }
}
