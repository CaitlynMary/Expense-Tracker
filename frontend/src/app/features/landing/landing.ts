import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Wallet, ArrowRight, LogIn, UserPlus, Sparkles, PieChart, TrendingUp } from 'lucide-angular';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './landing.html',
  styleUrls: ['./landing.scss']
})
export class Landing {
  readonly icons = { Wallet, ArrowRight, LogIn, UserPlus, Sparkles, PieChart, TrendingUp };
}
