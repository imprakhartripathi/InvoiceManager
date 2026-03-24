import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, map, takeUntil } from 'rxjs/operators';

import { AuthService } from '../../auth/auth.service';
import { LoadingService } from '../../services/loading.service';

@Component({
  standalone: false,
  selector: 'app-shell',
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss']
})
export class AppShellComponent implements OnInit, OnDestroy {
  readonly isMobile = signal(false);
  readonly pageTitle = signal('Dashboard');
  readonly isPageLoading;

  readonly navItems = [
    { label: 'Dashboard', icon: 'space_dashboard', route: '/dashboard' },
    { label: 'Templates', icon: 'view_quilt', route: '/templates' },
    { label: 'Invoices', icon: 'receipt_long', route: '/invoices' },
    { label: 'SMTP Settings', icon: 'settings', route: '/settings' }
  ];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly breakpointObserver: BreakpointObserver,
    private readonly loadingService: LoadingService
  ) {
    this.isPageLoading = this.loadingService.isLoading;
  }

  ngOnInit(): void {
    this.breakpointObserver
      .observe('(max-width: 960px)')
      .pipe(
        map((state) => state.matches),
        takeUntil(this.destroy$)
      )
      .subscribe((mobile) => this.isMobile.set(mobile));

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((event) => this.pageTitle.set(this.resolveTitle(event.urlAfterRedirects)));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get userEmail(): string {
    return this.authService.user()?.email ?? 'Signed in user';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/auth/login');
  }

  private resolveTitle(url: string): string {
    if (url.startsWith('/templates/new')) return 'Template Builder';
    if (url.startsWith('/templates')) return 'Templates';
    if (url.startsWith('/invoices/new')) return 'Create Invoice';
    if (url.startsWith('/invoices')) return 'Invoices';
    if (url.startsWith('/settings')) return 'SMTP Settings';
    return 'Dashboard';
  }
}
