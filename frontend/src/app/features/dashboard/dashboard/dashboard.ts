import { ChangeDetectorRef, Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';
import { NgxEchartsModule } from 'ngx-echarts';
import { EChartsOption } from 'echarts';
import { forkJoin, Subscription } from 'rxjs';
import { finalize, map } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { ReportDateRange, ReportService } from '../../../core/services/report.service';
import { PageResponse } from '../../../shared/models/api.model';
import { Bill } from '../../../shared/models/bill.model';
import { Expense } from '../../../shared/models/expense.model';
import { CategoryReport, DashboardSummary } from '../../../shared/models/report.model';
import { ThemeService, ThemeMode } from '../../../core/services/theme.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxEchartsModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class Dashboard implements OnInit {
  private apiService = inject(ApiService);
  private reportService = inject(ReportService);
  private cdr = inject(ChangeDetectorRef);
  private themeService = inject(ThemeService);

  isLoading = true;
  errorMessage = '';
  summary: DashboardSummary | null = null;
  categoryReport: CategoryReport[] = [];
  expenses: Expense[] = [];
  chartOption: EChartsOption = {};

  selectedFilter = 'THIS_MONTH';
  filterOptions: { value: string; label: string }[] = [];

  private themeSub?: Subscription;
  private currentTheme: ThemeMode = 'light';

  ngOnInit() {
    this.themeSub = this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
      if (this.categoryReport.length > 0) {
        this.setupChart(this.categoryReport);
        this.cdr.markForCheck();
      }
    });

    this.filterOptions = this.generateFilterOptions();
    this.loadDashboardData();
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

  loadDashboardData() {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    const range = this.getSelectedRange();
    if (!range) {
      this.isLoading = false;
      this.cdr.markForCheck();
      return;
    }
    const expenseParams = new HttpParams()
      .set('startDate', range.startDate)
      .set('endDate', range.endDate)
      .set('page', '0')
      .set('size', '200')
      .set('sortBy', 'expenseDate')
      .set('sortDir', 'desc');

    forkJoin({
      summary: this.reportService.getDashboardSummary(range),
      categories: this.reportService.getCategoryWise(range),
      expenses: this.apiService.get<PageResponse<Expense>>('/expenses/filter/date', expenseParams).pipe(
        map((response) => response.content ?? [])
      )
    }).pipe(finalize(() => {
      this.isLoading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: ({ summary, categories, expenses }) => {
        this.summary = summary;
        this.categoryReport = categories ?? [];
        this.expenses = expenses ?? [];
        this.setupChart(this.categoryReport);
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('[Dashboard] Failed to load dashboard data', error);
        this.errorMessage = this.getErrorMessage(error);
        this.setupChart([]);
        this.cdr.markForCheck();
      }
    });
  }

  get overviewTitle(): string {
    return `${this.currentMonthLabel} Overview`;
  }

  get currentMonthLabel(): string {
    return this.filterOptions.find(option => option.value === this.selectedFilter)?.label || 'This Month';
  }

  onFilterChange() {
    this.loadDashboardData();
  }

  get totalExpense(): number {
    return this.toNumber(this.summary?.spentThisMonth ?? this.summary?.totalExpense);
  }

  get monthlyBudget(): number {
    const directBudget = this.toNumber(this.summary?.monthlyBudget ?? this.summary?.totalBudget);
    if (directBudget > 0) {
      return directBudget;
    }

    const derivedBudget = this.toNumber(this.summary?.remainingBudget) + this.totalExpense;
    return derivedBudget > 0 ? derivedBudget : 0;
  }

  get remainingBudget(): number {
    return this.remainingBudgetRaw;
  }

  get pendingBillsCount(): number {
    return this.toNumber(this.summary?.pendingBillsCount ?? this.summary?.upcomingBillsCount);
  }

  get pendingBillsAmount(): number {
    return this.toNumber(this.summary?.pendingBillsAmount);
  }

  get savingsSavedAmount(): number {
    return this.toNumber(this.summary?.savingsSavedAmount);
  }

  get savingsTargetAmount(): number {
    return this.toNumber(this.summary?.savingsTargetAmount);
  }

  get savingsProgress(): number {
    return this.toNumber(this.summary?.savingsProgress);
  }

  get topCategoryText(): string {
    const bestCategory = [...this.categoryReport]
      .sort((left, right) => this.toNumber(right.amount) - this.toNumber(left.amount))[0];

    if (!bestCategory) {
      return 'No expenses this month';
    }

    return `${bestCategory.category} - ${this.formatCurrency(bestCategory.amount)}`;
  }

  get highestExpenseText(): string {
    const highestExpense = [...this.expenses]
      .sort((left, right) => this.toNumber(right.amount) - this.toNumber(left.amount))[0];

    if (!highestExpense) {
      return 'No expenses this month';
    }

    return `${highestExpense.title} - ${this.formatCurrency(highestExpense.amount)}`;
  }

  get billDueSoonText(): string {
    const nextBill = this.pendingBillItems[0];
    if (!nextBill?.name || !nextBill.dueDate) {
      return 'No pending bills this month';
    }

    return `${nextBill.name} - ${this.formatMonthDay(nextBill.dueDate)}`;
  }

  formatCurrency(value: number): string {
    return `\u20B9${this.toNumber(value).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
  }

  private get remainingBudgetRaw(): number {
    const directRemaining = this.toNumber(this.summary?.remainingBudget);
    if (directRemaining !== 0) {
      return directRemaining;
    }

    const directBudget = this.toNumber(this.summary?.monthlyBudget ?? this.summary?.totalBudget);
    if (directBudget > 0) {
      return directBudget - this.totalExpense;
    }

    return 0;
  }

  private get pendingBillItems(): Bill[] {
    const pendingBills = this.summary?.pendingBills;
    return Array.isArray(pendingBills) ? pendingBills : [];
  }

  private setupChart(categories: CategoryReport[]) {
    const data = categories.map((item) => ({
      name: item.category || 'Uncategorized',
      value: this.toNumber(item.amount)
    }));

    const isDark = this.currentTheme === 'dark';
    const textColor = isDark ? '#F5F3FA' : '#1C1628';
    const bgColor = isDark ? '#221D30' : '#FFFFFF';
    const borderColor = isDark ? '#8D73E6' : '#7055C0';
    const axisColor = isDark ? '#A59DBD' : '#857A9E';

    this.chartOption = {
      color: ['#7055C0'],
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          const point = Array.isArray(params) ? params[0] : params;
          return `${point.name}<br/>${this.formatCurrency(point.value)}`;
        },
        backgroundColor: bgColor,
        borderColor: borderColor,
        textStyle: { color: textColor }
      },
      grid: {
        left: 12,
        right: 16,
        top: 16,
        bottom: 12,
        containLabel: true
      },
      xAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => this.formatCurrency(value),
          color: axisColor
        },
        splitLine: {
          lineStyle: { color: 'rgba(112, 85, 192, 0.1)' }
        }
      },
      yAxis: {
        type: 'category',
        data: data.map((item) => item.name),
        axisLabel: { 
          width: 120, 
          overflow: 'truncate',
          color: axisColor
        }
      },
      series: [{
        name: 'Expenses',
        type: 'bar',
        data: data.map((item) => item.value),
        barMaxWidth: 28,
        itemStyle: { 
          borderRadius: [0, 6, 6, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#7055C0' },
              { offset: 1, color: '#A98FD8' }
            ]
          }
        }
      }]
    };
  }

  private getErrorMessage(error: any): string {
    if (error?.status === 0) {
      return 'Cannot connect to backend. Make sure Spring Boot is running on http://localhost:8080.';
    }

    if (error?.status === 401 || error?.status === 403) {
      return 'Your session is invalid or expired. Please log in again.';
    }

    return error?.error?.message || 'Unable to load dashboard data from the backend.';
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
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatMonthDay(dateValue: string): string {
    const [year, month, day] = dateValue.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return date.toLocaleString('en-US', { month: 'long', day: 'numeric' });
  }

  private toNumber(value: unknown): number {
    const numericValue = typeof value === 'string' ? Number(value) : Number(value ?? 0);
    return Number.isFinite(numericValue) ? numericValue : 0;
  }
}
