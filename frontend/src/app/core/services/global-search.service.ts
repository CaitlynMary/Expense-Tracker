import { Injectable, inject } from '@angular/core';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { Bill } from '../../shared/models/bill.model';
import { Expense } from '../../shared/models/expense.model';
import { BillService } from './bill.service';
import { ExpenseService } from './expense.service';

export type GlobalSearchResultType = 'expense' | 'bill';

export interface GlobalSearchResult {
  id: number;
  type: GlobalSearchResultType;
  title: string;
  subtitle: string;
  amount: number;
  route: '/expense' | '/bill';
  matchText: string;
}

interface SearchDataset {
  expenses: Expense[];
  bills: Bill[];
}

@Injectable({ providedIn: 'root' })
export class GlobalSearchService {
  private expenseService = inject(ExpenseService);
  private billService = inject(BillService);

  private dataset$?: Observable<SearchDataset>;

  getResults(query: string): Observable<GlobalSearchResult[]> {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) {
      return of([]);
    }

    return this.getDataset().pipe(
      map(({ expenses, bills }) => {
        const expenseResults = expenses
          .filter((expense) => this.matchesExpense(expense, normalizedQuery))
          .map((expense) => this.toExpenseResult(expense));

        const billResults = bills
          .filter((bill) => this.matchesBill(bill, normalizedQuery))
          .map((bill) => this.toBillResult(bill));

        return [...expenseResults, ...billResults]
          .sort((left, right) => left.title.localeCompare(right.title))
          .slice(0, 8);
      })
    );
  }

  refresh(): void {
    this.dataset$ = undefined;
  }

  private getDataset(): Observable<SearchDataset> {
    if (!this.dataset$) {
      this.dataset$ = forkJoin({
        expenses: this.expenseService.getAll().pipe(catchError(() => of([]))),
        bills: this.billService.getAll().pipe(catchError(() => of([])))
      }).pipe(
        tap((dataset) => {
          console.log('[GlobalSearch] Loaded searchable dataset.', {
            expenses: dataset.expenses.length,
            bills: dataset.bills.length
          });
        }),
        shareReplay(1)
      );
    }

    return this.dataset$;
  }

  private matchesExpense(expense: Expense, query: string): boolean {
    return this.collectExpenseTerms(expense).some((term) => term.includes(query));
  }

  private matchesBill(bill: Bill, query: string): boolean {
    return this.collectBillTerms(bill).some((term) => term.includes(query));
  }

  private collectExpenseTerms(expense: Expense): string[] {
    return [
      expense.title,
      expense.category,
      expense.paymentMethod,
      expense.notes,
      this.amountText(expense.amount)
    ].map((value) => (value ?? '').toString().toLowerCase());
  }

  private collectBillTerms(bill: Bill): string[] {
    return [
      bill.name,
      bill.billType,
      bill.notes,
      this.amountText(bill.amount)
    ].map((value) => (value ?? '').toString().toLowerCase());
  }

  private toExpenseResult(expense: Expense): GlobalSearchResult {
    return {
      id: expense.id ?? 0,
      type: 'expense',
      title: expense.title,
      subtitle: expense.category || expense.paymentMethod,
      amount: expense.amount,
      route: '/expense',
      matchText: `Expense ${expense.title} ${expense.category} ${expense.paymentMethod} ${expense.notes ?? ''}`
    };
  }

  private toBillResult(bill: Bill): GlobalSearchResult {
    return {
      id: bill.id ?? 0,
      type: 'bill',
      title: bill.name,
      subtitle: bill.billType === 'VARIABLE' ? 'Variable Bill' : 'Bill',
      amount: bill.amount,
      route: '/bill',
      matchText: `Bill ${bill.name} ${bill.billType ?? ''} ${bill.notes ?? ''}`
    };
  }

  private amountText(amount: number): string {
    const numeric = Number(amount ?? 0);
    return Number.isFinite(numeric) ? numeric.toString() : '';
  }
}
