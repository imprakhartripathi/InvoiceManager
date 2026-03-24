import { Component, OnInit, signal } from '@angular/core';

import { InvoiceDto, InvoiceService } from '../../invoice.service';

@Component({
  standalone: false,
  selector: 'app-invoice-list',
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.scss']
})
export class InvoiceListComponent implements OnInit {
  readonly invoices = signal<InvoiceDto[]>([]);
  loading = false;
  error = '';

  constructor(private readonly invoiceService: InvoiceService) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.loading = true;
    this.invoiceService.list().subscribe({
      next: (list) => {
        this.error = '';
        this.invoices.set(list);
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load invoices.';
        this.loading = false;
      }
    });
  }
}
