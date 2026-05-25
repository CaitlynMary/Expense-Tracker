import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Category } from '../../shared/models/category.model';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../../shared/models/api.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiService = inject(ApiService);
  private readonly path = '/categories';

  getAll(): Observable<Category[]> {
    return this.apiService.get<ApiResponse<Category[]>>(this.path).pipe(map(response => response.data ?? []));
  }

  getById(id: number): Observable<Category> {
    return this.apiService.get<ApiResponse<Category>>(`${this.path}/${id}`).pipe(map(response => response.data));
  }

  create(category: Category): Observable<Category> {
    return this.apiService.post<ApiResponse<Category>>(this.path, category).pipe(map(response => response.data));
  }

  update(id: number, category: Category): Observable<Category> {
    return this.apiService.put<ApiResponse<Category>>(`${this.path}/${id}`, category).pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.path}/${id}`);
  }
}
