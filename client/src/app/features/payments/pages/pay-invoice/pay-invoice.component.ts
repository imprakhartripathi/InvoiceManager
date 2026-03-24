import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { switchMap } from 'rxjs/operators';

import { InvoiceService } from '../../../invoices/invoice.service';
import { PaymentService } from '../../payment.service';

type RazorpayOrder = { orderId: string; keyId: string; amountInPaise: number; currency: string };

declare global {
  interface Window {
    Razorpay?: new (options: Record<string, unknown>) => { open: () => void };
  }
}

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
        this.openRazorpayCheckout(order);
      },
      error: (err) => {
        this.creatingOrder = false;
        this.error = err?.error?.message ?? 'Unable to create payment order.';
      }
    });
  }

  private openRazorpayCheckout(order: RazorpayOrder): void {
    const invoice = this.summary();
    if (!invoice) {
      return;
    }
    if (!order.keyId) {
      this.error = 'Razorpay key is missing on server configuration.';
      return;
    }
    if (!window.Razorpay) {
      this.error = 'Razorpay checkout failed to load. Refresh and try again.';
      return;
    }

    const options: Record<string, unknown> = {
      key: order.keyId,
      amount: order.amountInPaise,
      currency: order.currency,
      name: 'Invoice Manager',
      description: `Invoice ${invoice.invoiceId}`,
      order_id: order.orderId,
      handler: () => {
        this.message = 'Payment completed. Status will update after webhook confirmation.';
        this.error = '';
      },
      modal: {
        ondismiss: () => {
          this.message = '';
          this.error = 'Payment popup closed before completion.';
        }
      },
      theme: {
        color: '#0b5fc0'
      }
    };

    const checkout = new window.Razorpay(options);
    checkout.open();
  }
}
