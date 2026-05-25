import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, OnInit, Output, ViewChild, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { LucideAngularModule, Menu, Bell, Search, User } from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';
import { GlobalSearchResult, GlobalSearchService } from '../../core/services/global-search.service';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationDto } from '../../shared/models/notification.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss']
})
export class Navbar implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  private elementRef = inject(ElementRef<HTMLElement>);
  private router = inject(Router);
  private authService = inject(AuthService);
  private globalSearchService = inject(GlobalSearchService);

  @Output() toggleSidebarEvent = new EventEmitter<void>();
  @ViewChild('searchInput') searchInput?: ElementRef<HTMLInputElement>;

  readonly icons = { Menu, Bell, Search, User };
  readonly searchControl = new FormControl('', { nonNullable: true });
  isSearchOpen = false;
  isSearching = false;
  searchResults: GlobalSearchResult[] = [];
  hasSearched = false;

  notifications: NotificationDto[] = [];
  unreadCount = 0;
  isNotificationsOpen = false;
  private readonly READ_NOTIFS_KEY = 'read_notifications';

  private notificationService = inject(NotificationService);

  ngOnInit() {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((query) => {
        const trimmedQuery = query.trim();
        this.hasSearched = !!trimmedQuery;
        this.isSearching = !!trimmedQuery;

        if (!trimmedQuery) {
          this.searchResults = [];
          this.isSearchOpen = false;
          this.isSearching = false;
          this.cdr.markForCheck();
          return of([]);
        }

        this.isSearchOpen = true;
        this.cdr.markForCheck();
        return this.globalSearchService.getResults(trimmedQuery);
      })
    ).subscribe((results) => {
      this.searchResults = results;
      this.isSearching = false;
      this.isSearchOpen = this.hasSearched;
      this.cdr.markForCheck();
    });

    this.loadNotifications();
  }

  loadNotifications() {
    this.notificationService.getNotifications().subscribe({
      next: (notifs) => {
        this.notifications = notifs;
        this.calculateUnreadCount();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load notifications', err);
      }
    });
  }

  calculateUnreadCount() {
    try {
      const readIdsStr = localStorage.getItem(this.READ_NOTIFS_KEY);
      const readIds: string[] = readIdsStr ? JSON.parse(readIdsStr) : [];
      this.unreadCount = this.notifications.filter(n => !readIds.includes(n.id)).length;
    } catch (e) {
      this.unreadCount = this.notifications.length;
    }
  }

  toggleNotifications(event: MouseEvent) {
    event.stopPropagation();
    this.isNotificationsOpen = !this.isNotificationsOpen;
    if (this.isNotificationsOpen) {
      this.isSearchOpen = false;
      this.markAllAsRead();
    }
    this.cdr.markForCheck();
  }

  markAllAsRead() {
    if (this.notifications.length === 0) return;
    
    try {
      const readIdsStr = localStorage.getItem(this.READ_NOTIFS_KEY);
      let readIds: string[] = readIdsStr ? JSON.parse(readIdsStr) : [];
      
      const newIds = this.notifications.map(n => n.id);
      readIds = [...new Set([...readIds, ...newIds])];
      
      localStorage.setItem(this.READ_NOTIFS_KEY, JSON.stringify(readIds));
      this.unreadCount = 0;
      this.cdr.markForCheck();
    } catch (e) {
      console.error('Failed to save read notifications', e);
    }
  }

  get displayUserName(): string {
    return this.authService.currentUser()?.name || 'User';
  }

  get displayUserEmail(): string {
    return this.authService.currentUser()?.email || '';
  }

  toggleSidebar() {
    this.toggleSidebarEvent.emit();
  }

  openSearch() {
    if (this.searchControl.value.trim()) {
      this.isSearchOpen = true;
      this.cdr.markForCheck();
    }
  }

  selectResult(result: GlobalSearchResult) {
    this.searchControl.setValue(result.title, { emitEvent: false });
    this.searchResults = [];
    this.hasSearched = false;
    this.isSearchOpen = false;
    this.cdr.markForCheck();

    void this.router.navigate([result.route], {
      queryParams: {
        highlightId: result.id,
        highlightType: result.type
      }
    });
  }

  trackResult(_: number, result: GlobalSearchResult): string {
    return `${result.type}-${result.id}`;
  }

  resultIcon(result: GlobalSearchResult): string {
    return result.type === 'bill' ? '\uD83D\uDCC4' : '\uD83D\uDCB8';
  }

  resultLabel(result: GlobalSearchResult): string {
    return result.type === 'bill' ? 'Bill' : 'Expense';
  }

  formatAmount(amount: number): string {
    return `\u20B9${Number(amount ?? 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.isSearchOpen = false;
      this.isNotificationsOpen = false;
      this.cdr.markForCheck();
    }
  }
}
