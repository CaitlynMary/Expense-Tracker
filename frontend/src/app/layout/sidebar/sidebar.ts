import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LucideAngularModule, LayoutDashboard, Receipt, Tags, Wallet, ShoppingCart, PiggyBank, Calendar, CreditCard, PieChart, Settings, LogOut } from 'lucide-angular';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss']
})
export class Sidebar {
  @Input() isCollapsed = false;

  readonly icons = {
    LayoutDashboard, Receipt, Tags, Wallet, ShoppingCart, PiggyBank, Calendar, CreditCard, PieChart, Settings, LogOut
  };

  menuItems = [
    { label: 'Dashboard', icon: this.icons.LayoutDashboard, route: '/dashboard' },
    { label: 'Expenses', icon: this.icons.Receipt, route: '/expense' },
    { label: 'Categories', icon: this.icons.Tags, route: '/category' },
    { label: 'Budgets', icon: this.icons.Wallet, route: '/budget' },
    { label: 'Groceries', icon: this.icons.ShoppingCart, route: '/grocery' },
    { label: 'Savings', icon: this.icons.PiggyBank, route: '/savings' },
    { label: 'Bills', icon: this.icons.Calendar, route: '/bill' },
    { label: 'Payment Methods', icon: this.icons.CreditCard, route: '/payment-methods' },
    { label: 'Reports', icon: this.icons.PieChart, route: '/reports' }
  ];

  private authService = inject(AuthService);

  logout() {
    this.authService.logout();
  }
}
