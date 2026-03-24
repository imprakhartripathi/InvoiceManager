import { Routes } from '@angular/router';

import { AppShellComponent } from './core/layout/app-shell/app-shell.component';

// Secondary route map kept in sync with AppRoutingModule for standalone migration readiness.
export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      { path: 'dashboard', loadChildren: () => import('./features/dashboard/dashboard.module').then((m) => m.DashboardModule) },
      { path: 'templates', loadChildren: () => import('./features/templates/templates.module').then((m) => m.TemplatesModule) },
      { path: 'invoices', loadChildren: () => import('./features/invoices/invoices.module').then((m) => m.InvoicesModule) },
      { path: 'settings', loadChildren: () => import('./features/settings/settings.module').then((m) => m.SettingsModule) }
    ]
  },
  { path: 'auth', loadChildren: () => import('./features/auth/auth.module').then((m) => m.AuthModule) },
  { path: 'pay', loadChildren: () => import('./features/payments/payments.module').then((m) => m.PaymentsModule) }
];
