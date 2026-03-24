import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../core/services/api.service';

export interface InvoiceDto {
  id: string;
  userId: string;
  templateId: string;
  data: Record<string, unknown>;
  lineItems: Record<string, unknown>[];
  total: number;
  status: 'DRAFT' | 'SENT' | 'PAID';
  razorpayOrderId: string | null;
  createdAt: string;
  paidAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<InvoiceDto[]> {
    return this.api.get<InvoiceDto[]>('/api/invoices');
  }

  create(payload: {
    templateId: string;
    data: Record<string, unknown>;
    lineItems: Record<string, unknown>[];
  }): Observable<InvoiceDto> {
    return this.api.post<InvoiceDto>('/api/invoices', payload);
  }

  getPublicSummary(invoiceId: string) {
    return this.api.get<{ invoiceId: string; templateName: string; total: number; status: string; razorpayOrderId: string }>(
      `/api/public/invoices/${invoiceId}/summary`
    );
  }
}
