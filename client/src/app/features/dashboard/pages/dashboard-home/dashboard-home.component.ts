import { Component, OnInit, signal } from '@angular/core';

import { AuthService } from '../../../../core/auth/auth.service';
import { InvoiceService } from '../../../invoices/invoice.service';
import { TemplateService } from '../../../templates/template.service';

@Component({
  standalone: false,
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.component.html',
  styleUrls: ['./dashboard-home.component.scss']
})
export class DashboardHomeComponent implements OnInit {
  readonly totalTemplates = signal(0);
  readonly totalInvoices = signal(0);
  readonly draftInvoices = signal(0);
  readonly paidInvoices = signal(0);
  readonly loading = signal(true);

  constructor(
    private readonly authService: AuthService,
    private readonly templateService: TemplateService,
    private readonly invoiceService: InvoiceService
  ) {}

  get userEmail(): string {
    return this.authService.user()?.email ?? 'team';
  }

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading.set(true);

    this.templateService.list().subscribe({
      next: (templates) => this.totalTemplates.set(templates.length),
      error: () => this.totalTemplates.set(0)
    });

    this.invoiceService.list().subscribe({
      next: (invoices) => {
        this.totalInvoices.set(invoices.length);
        this.draftInvoices.set(invoices.filter((item) => item.status === 'DRAFT').length);
        this.paidInvoices.set(invoices.filter((item) => item.status === 'PAID').length);
        this.loading.set(false);
      },
      error: () => {
        this.totalInvoices.set(0);
        this.draftInvoices.set(0);
        this.paidInvoices.set(0);
        this.loading.set(false);
      }
    });
  }
}
