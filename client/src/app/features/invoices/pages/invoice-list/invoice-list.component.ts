import { Component, OnInit, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { TemplateDto, TemplateService } from '../../../templates/template.service';
import { InvoiceDetailsDialogComponent } from '../../components/invoice-details-dialog/invoice-details-dialog.component';
import { InvoiceDto, InvoiceService } from '../../invoice.service';

@Component({
  standalone: false,
  selector: 'app-invoice-list',
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.scss']
})
export class InvoiceListComponent implements OnInit {
  readonly invoices = signal<InvoiceDto[]>([]);
  readonly filteredInvoices = signal<InvoiceDto[]>([]);
  readonly templates = signal<TemplateDto[]>([]);

  statusFilter = '';
  templateFilter = '';
  dateFilter = '';

  loading = false;
  error = '';

  constructor(
    private readonly invoiceService: InvoiceService,
    private readonly templateService: TemplateService,
    private readonly dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadTemplates();
    this.loadInvoices();
  }

  loadTemplates(): void {
    this.templateService.list().subscribe({
      next: (templates) => this.templates.set(templates),
      error: () => this.templates.set([])
    });
  }

  loadInvoices(): void {
    this.loading = true;
    this.invoiceService.list().subscribe({
      next: (list) => {
        this.error = '';
        this.invoices.set(list);
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load invoices.';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    const filtered = this.invoices().filter((invoice) => {
      const statusOk = !this.statusFilter || invoice.status === this.statusFilter;
      const templateOk = !this.templateFilter || invoice.templateId === this.templateFilter;
      const dateOk = !this.dateFilter || invoice.createdAt.slice(0, 10) === this.dateFilter;
      return statusOk && templateOk && dateOk;
    });
    this.filteredInvoices.set(filtered);
  }

  resetFilters(): void {
    this.statusFilter = '';
    this.templateFilter = '';
    this.dateFilter = '';
    this.applyFilters();
  }

  templateName(templateId: string): string {
    return this.templates().find((t) => t.id === templateId)?.name ?? 'Unknown Template';
  }

  topFields(invoice: InvoiceDto): string[] {
    const template = this.templates().find((t) => t.id === invoice.templateId);
    const orderedKeys = (template?.fields ?? []).map((field) => field.key);
    const selectedKeys = orderedKeys.length > 0 ? orderedKeys.slice(0, 2) : Object.keys(invoice.data ?? {}).slice(0, 2);
    return selectedKeys.map((key) => `${key}: ${String(invoice.data?.[key] ?? '-')}`);
  }

  openDetails(invoice: InvoiceDto): void {
    this.dialog.open(InvoiceDetailsDialogComponent, {
      width: '640px',
      data: {
        invoice,
        templateName: this.templateName(invoice.templateId),
        previewFields: this.topFields(invoice)
      }
    });
  }

  markSent(invoice: InvoiceDto): void {
    this.invoiceService.markSent(invoice.id).subscribe({
      next: () => this.loadInvoices(),
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to send invoice.';
      }
    });
  }

  markPaidCash(invoice: InvoiceDto): void {
    const confirmed = window.confirm('Confirm cash has been received for this invoice?');
    if (!confirmed) {
      return;
    }

    this.invoiceService.markPaidCash(invoice.id).subscribe({
      next: () => this.loadInvoices(),
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to mark invoice as PAID by cash.';
      }
    });
  }

  printInvoice(invoice: InvoiceDto): void {
    const template = this.templateName(invoice.templateId);
    const rows = Object.entries(invoice.data ?? {})
      .map(
        ([key, value]) =>
          `<tr><td style="padding:8px;border:1px solid #ddd">${key}</td><td style="padding:8px;border:1px solid #ddd">${value ?? '-'}</td></tr>`
      )
      .join('');

    const html = `
      <html>
        <head><title>Invoice ${invoice.id}</title></head>
        <body style="font-family:Arial,sans-serif;padding:24px;">
          <h2>Invoice ${invoice.id}</h2>
          <p><strong>Template:</strong> ${template}</p>
          <p><strong>Display Name:</strong> ${invoice.displayName}</p>
          <p><strong>Status:</strong> ${invoice.status}</p>
          <p><strong>Total:</strong> ${invoice.total}</p>
          <table style="border-collapse:collapse;width:100%;margin-top:16px;">${rows}</table>
        </body>
      </html>
    `;

    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) {
      this.error = 'Unable to open print window. Please allow popups.';
      return;
    }
    printWindow.document.open();
    printWindow.document.write(html);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => printWindow.print(), 200);
  }
}
