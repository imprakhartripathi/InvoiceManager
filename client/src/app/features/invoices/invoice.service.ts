import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../core/services/api.service';

export interface InvoiceDto {
  id: string;
  userId: string;
  templateId: string;
  displayName: string;
  data: Record<string, unknown>;
  lineItems: Record<string, unknown>[];
  total: number;
  status: 'DRAFT' | 'SENT' | 'PAID';
  razorpayOrderId: string | null;
  paidVia: 'CASH' | 'RAZORPAY' | null;
  paidBy: 'OWNER' | 'CLIENT' | null;
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
    displayName: string;
    data: Record<string, unknown>;
    lineItems: Record<string, unknown>[];
  }): Observable<InvoiceDto> {
    return this.api.post<InvoiceDto>('/api/invoices', payload);
  }

  update(invoiceId: string, payload: { displayName: string; data: Record<string, unknown>; lineItems: Record<string, unknown>[] }) {
    return this.api.put<InvoiceDto>(`/api/invoices/${invoiceId}`, payload);
  }

  remove(invoiceId: string) {
    return this.api.delete<void>(`/api/invoices/${invoiceId}`);
  }

  getOne(invoiceId: string): Observable<InvoiceDto> {
    return this.api.get<InvoiceDto>(`/api/invoices/${invoiceId}`);
  }

  markSent(invoiceId: string) {
    return this.api.post<InvoiceDto>(`/api/invoices/${invoiceId}/send`, {});
  }

  markPaidCash(invoiceId: string) {
    return this.api.post<InvoiceDto>(`/api/invoices/${invoiceId}/pay/cash`, {});
  }

  getPublicSummary(invoiceId: string) {
    return this.api.get<{ invoiceId: string; templateName: string; total: number; status: string; razorpayOrderId: string }>(
      `/api/public/invoices/${invoiceId}/summary`
    );
  }
}
