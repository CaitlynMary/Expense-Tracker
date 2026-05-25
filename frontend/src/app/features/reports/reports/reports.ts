import { ChangeDetectorRef, Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxEchartsModule } from 'ngx-echarts';
import { EChartsOption } from 'echarts';
import { forkJoin, Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { HttpParams } from '@angular/common/http';
import { ReportDateRange, ReportService } from '../../../core/services/report.service';
import { CategoryReport, DashboardSummary } from '../../../shared/models/report.model';
import { ThemeService, ThemeMode } from '../../../core/services/theme.service';
import { Expense } from '../../../shared/models/expense.model';
import { ApiService } from '../../../core/services/api.service';
import { PageResponse } from '../../../shared/models/api.model';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxEchartsModule],
  templateUrl: './reports.html',
  styleUrls: ['./reports.scss']
})
export class Reports implements OnInit {
  private reportService = inject(ReportService);
  private apiService = inject(ApiService);
  private cdr = inject(ChangeDetectorRef);
  private themeService = inject(ThemeService);

  summary?: DashboardSummary;
  categoryReport: CategoryReport[] = [];
  expenses: Expense[] = [];

  chartOption: EChartsOption = {};
  isLoading = false;
  errorMessage = '';
  selectedFilter = 'THIS_MONTH';

  filterOptions: { value: string; label: string }[] = [];

  // Computed values
  topCategory = '';
  totalSpent = 0;
  billsPaid = 0;
  moneySaved = 0;

  private themeSub?: Subscription;
  private currentTheme: ThemeMode = 'light';

  ngOnInit() {
    this.themeSub = this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
      if (this.categoryReport.length > 0) {
        this.chartOption = this.buildCategoryChart(this.categoryReport);
        this.cdr.markForCheck();
      }
    });

    this.filterOptions = this.generateFilterOptions();
    this.loadReports();
  }

  ngOnDestroy() {
    this.themeSub?.unsubscribe();
  }

  generateFilterOptions() {
    const options: { value: string; label: string }[] = [
      { value: 'THIS_MONTH', label: 'This Month' },
      { value: 'LAST_MONTH', label: 'Last Month' }
    ];

    const today = new Date();
    for (let i = 2; i < 12; i++) {
      const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
      const monthLabel = d.toLocaleString('en-US', { month: 'long', year: 'numeric' });
      const yearStr = d.getFullYear();
      const monthStr = String(d.getMonth() + 1).padStart(2, '0');
      const val = `MONTH_${yearStr}-${monthStr}`;
      options.push({ value: val, label: monthLabel });
    }

    return options;
  }

  loadReports() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const range = this.getSelectedRange();
    if (!range) {
      this.isLoading = false;
      this.cdr.markForCheck();
      return;
    }

    // Build params for expense date filter
    const expenseParams = new HttpParams()
      .set('startDate', range.startDate)
      .set('endDate', range.endDate)
      .set('page', '0')
      .set('size', '100')
      .set('sortBy', 'expenseDate')
      .set('sortDir', 'desc');

    forkJoin({
      summary: this.reportService.getDashboardSummary(range),
      categoryReport: this.reportService.getCategoryWise(range),
      expenses: this.apiService.get<PageResponse<Expense>>('/expenses/filter/date', expenseParams).pipe(
        map(response => response.content ?? [])
      )
    })
      .pipe(finalize(() => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: response => {
          this.summary = response.summary;
          this.categoryReport = response.categoryReport ?? [];
          this.expenses = response.expenses ?? [];

          // Compute simple values
          this.totalSpent = this.summary?.totalExpense || 0;
          this.billsPaid = this.summary?.billsPaidCount || 0;
          this.moneySaved = this.summary?.savingsAdded || 0;

          // Top spending category
          if (this.categoryReport.length > 0) {
            const sorted = [...this.categoryReport].sort((a, b) => b.amount - a.amount);
            this.topCategory = sorted[0].category;
          } else {
            this.topCategory = 'N/A';
          }

          this.chartOption = this.buildCategoryChart(this.categoryReport);
          this.cdr.markForCheck();
        },
        error: error => {
          console.error('[Reports] Failed to load reports', error);
          this.errorMessage = this.getErrorMessage(error, 'Unable to load reports.');
          this.chartOption = this.buildCategoryChart([]);
          this.cdr.markForCheck();
        }
      });
  }

  get hasCategoryData(): boolean {
    return this.categoryReport.length > 0;
  }

  get selectedFilterLabel(): string {
    return this.filterOptions.find(option => option.value === this.selectedFilter)?.label || 'This Month';
  }

  onFilterChange() {
    this.loadReports();
  }

  private buildCategoryChart(data: CategoryReport[]): EChartsOption {
    const isDark = this.currentTheme === 'dark';
    const textColor = isDark ? '#F5F3FA' : '#1C1628';
    const bgColor = isDark ? '#221D30' : '#FFFFFF';
    const borderColor = isDark ? '#8D73E6' : '#7055C0';
    const axisColor = isDark ? '#A59DBD' : '#857A9E';
    const itemBorderColor = isDark ? '#221D30' : '#FFFFFF';

    return {
      color: [
        '#7055C0', '#A98FD8', '#F59E0B', '#10B981',
        '#8B5CF6', '#3B82F6', '#EC4899', '#14B8A6'
      ],
      tooltip: {
        trigger: 'item',
        backgroundColor: bgColor,
        borderColor: borderColor,
        borderWidth: 1,
        textStyle: { color: textColor, fontSize: 13 },
        formatter: (params: any) => {
          return `<strong>${params.name}</strong><br/>₹${params.value.toLocaleString('en-IN')} (${params.percent}%)`;
        }
      },
      legend: {
        bottom: 0,
        left: 'center',
        textStyle: { color: axisColor, fontSize: 12 },
        itemWidth: 12,
        itemHeight: 12,
        itemGap: 16
      },
      series: [
        {
          name: 'Expense by Category',
          type: 'pie',
          radius: ['48%', '72%'],
          center: ['50%', '44%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
            borderColor: itemBorderColor,
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: textColor
            },
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(112, 85, 192, 0.4)'
            }
          },
          data: data.map(item => ({ name: item.category, value: item.amount }))
        }
      ]
    };
  }

  private getSelectedRange(): ReportDateRange | null {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth();

    if (this.selectedFilter === 'THIS_MONTH') {
      const start = new Date(year, month, 1);
      const end = new Date(year, month + 1, 0);
      return { startDate: this.toDateInput(start), endDate: this.toDateInput(end) };
    }

    if (this.selectedFilter === 'LAST_MONTH') {
      const start = new Date(year, month - 1, 1);
      const end = new Date(year, month, 0);
      return { startDate: this.toDateInput(start), endDate: this.toDateInput(end) };
    }

    if (this.selectedFilter.startsWith('MONTH_')) {
      const parts = this.selectedFilter.substring(6).split('-');
      const selectYear = parseInt(parts[0], 10);
      const selectMonthIdx = parseInt(parts[1], 10) - 1;
      const start = new Date(selectYear, selectMonthIdx, 1);
      const end = new Date(selectYear, selectMonthIdx + 1, 0);
      return { startDate: this.toDateInput(start), endDate: this.toDateInput(end) };
    }

    // Default to this month
    const start = new Date(year, month, 1);
    const end = new Date(year, month + 1, 0);
    return { startDate: this.toDateInput(start), endDate: this.toDateInput(end) };
  }

  private toDateInput(date: Date): string {
    const normalized = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    return normalized.toISOString().split('T')[0];
  }

  private getErrorMessage(error: any, fallback: string): string {
    if (error?.status === 0) {
      return 'Cannot connect to backend. Make sure Spring Boot is running on http://localhost:8080.';
    }
    if (error?.status === 401 || error?.status === 403) {
      return 'Your session is invalid or expired. Please log in again.';
    }
    return error?.error?.message || fallback;
  }
}
