import { Injectable } from '@angular/core';

import { ApiService } from '../../core/services/api.service';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  constructor(private readonly api: ApiService) {}

  createOrder(invoiceId: string) {
    return this.api.post<{ orderId: string; keyId: string; amountInPaise: number; currency: string }>('/payments/orders', {
      invoiceId
    });
  }

  createPublicOrder(invoiceId: string) {
    return this.api.post<{ orderId: string; keyId: string; amountInPaise: number; currency: string }>(
      '/payments/public/orders',
      {
        invoiceId
      }
    );
  }
}
