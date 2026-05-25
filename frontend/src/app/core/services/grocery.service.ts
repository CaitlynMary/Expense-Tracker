import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ApiResponse } from '../../shared/models/api.model';
import { GroceryItem } from '../../shared/models/grocery.model';

@Injectable({ providedIn: 'root' })
export class GroceryService {
  private apiService = inject(ApiService);
  private readonly path = '/groceries';
  private readonly legacyPath = '/grocery';

  getAll(): Observable<GroceryItem[]> {
    return this.getItems(this.path).pipe(
      catchError(() => this.getItems(`${this.legacyPath}?purchased=false`))
    );
  }

  getHistory(): Observable<GroceryItem[]> {
    return this.getItems(`${this.path}/history`).pipe(
      catchError(() => this.getItems(`${this.legacyPath}?purchased=true`))
    );
  }

  create(item: GroceryItem): Observable<GroceryItem> {
    return this.apiService.post<ApiResponse<GroceryItem>>(this.path, item).pipe(
      map(response => response.data),
      catchError(() => this.apiService.post<ApiResponse<GroceryItem>>(this.legacyPath, item).pipe(map(response => response.data)))
    );
  }

  update(id: number, item: GroceryItem): Observable<GroceryItem> {
    return this.apiService.put<ApiResponse<GroceryItem>>(`${this.path}/${id}`, item).pipe(
      map(response => response.data),
      catchError(() => this.apiService.put<ApiResponse<GroceryItem>>(`${this.legacyPath}/${id}`, item).pipe(map(response => response.data)))
    );
  }

  markPurchased(id: number): Observable<GroceryItem> {
    return this.apiService.put<ApiResponse<GroceryItem>>(`${this.path}/${id}/purchase`, {}).pipe(
      map(response => response.data),
      catchError(() => this.apiService.put<ApiResponse<GroceryItem>>(`${this.legacyPath}/${id}/purchase`, {}).pipe(map(response => response.data)))
    );
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`).pipe(
      catchError(() => this.apiService.delete<void>(`${this.legacyPath}/${id}`))
    );
  }

  private getItems(path: string): Observable<GroceryItem[]> {
    return this.apiService.get<ApiResponse<GroceryItem[]>>(path).pipe(map(response => response.data ?? []));
  }
}
