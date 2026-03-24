import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { switchMap } from 'rxjs/operators';

import { InvoiceService } from '../../../invoices/invoice.service';
import { PaymentService } from '../../payment.service';

@Component({
  standalone: false,
  selector: 'app-pay-invoice',
  templateUrl: './pay-invoice.component.html',
  styleUrls: ['./pay-invoice.component.scss']
})
export class PayInvoiceComponent implements OnInit {
  readonly summary = signal<{ invoiceId: string; templateName: string; total: number; status: string } | null>(null);
  readonly orderId = signal<string | null>(null);
  readonly orderAmount = signal<number | null>(null);
  readonly orderCurrency = signal<string | null>(null);
  loading = false;
  creatingOrder = false;
  error = '';
  message = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly invoiceService: InvoiceService,
    private readonly paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.route.paramMap
      .pipe(switchMap((params) => this.invoiceService.getPublicSummary(params.get('invoiceId') ?? '')))
      .subscribe({
        next: (summary) => {
          this.loading = false;
          this.error = '';
          this.summary.set(summary);
        },
        error: () => {
          this.loading = false;
          this.error = 'Could not load invoice summary.';
        }
      });
  }

  createOrder(): void {
    const invoiceId = this.summary()?.invoiceId;
    if (!invoiceId) {
      return;
    }

    this.creatingOrder = true;
    this.paymentService.createPublicOrder(invoiceId).subscribe({
      next: (order) => {
        this.creatingOrder = false;
        this.error = '';
        this.orderId.set(order.orderId);
        this.orderAmount.set(order.amountInPaise / 100);
        this.orderCurrency.set(order.currency);
        this.message = 'Payment order created. Use this order with Razorpay checkout on your integration hook.';
      },
      error: () => {
        this.creatingOrder = false;
        this.error = 'Unable to create payment order.';
      }
    });
  }
}
