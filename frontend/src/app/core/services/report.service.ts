import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { BudgetUsage, CategoryReport, DashboardSummary, MonthlyReport, SavingsReport, MonthlyTrend } from '../../shared/models/report.model';

export interface ReportDateRange {
  startDate: string;
  endDate: string;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiService = inject(ApiService);
  private readonly path = '/reports';

  getDashboardSummary(range?: ReportDateRange): Observable<DashboardSummary> {
    return this.apiService.get<DashboardSummary>(`${this.path}/dashboard-summary`, this.toParams(range));
  }

  getMonthly(range?: ReportDateRange): Observable<MonthlyReport> {
    return this.apiService.get<MonthlyReport>(`${this.path}/monthly`, this.toParams(range));
  }

  getCategoryWise(range?: ReportDateRange): Observable<CategoryReport[]> {
    return this.apiService.get<CategoryReport[]>(`${this.path}/category-wise`, this.toParams(range));
  }

  getBudgetUsage(range?: ReportDateRange): Observable<BudgetUsage[]> {
    return this.apiService.get<BudgetUsage[]>(`${this.path}/budget-usage`, this.toParams(range));
  }

  getSavings(): Observable<SavingsReport> {
    return this.apiService.get<SavingsReport>(`${this.path}/savings`);
  }

  getTrend(endDate: string): Observable<MonthlyTrend[]> {
    let params = new HttpParams().set('endDate', endDate);
    return this.apiService.get<MonthlyTrend[]>(`${this.path}/trend`, params);
  }

  private toParams(range?: ReportDateRange): HttpParams {
    let params = new HttpParams();
    if (!range) {
      return params;
    }
    return params
      .set('startDate', range.startDate)
      .set('endDate', range.endDate);
  }
}
