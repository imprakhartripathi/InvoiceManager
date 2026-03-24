import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { SharedModule } from '../../shared/shared.module';
import { PayInvoiceComponent } from './pages/pay-invoice/pay-invoice.component';
import { PaymentsRoutingModule } from './payments-routing.module';

@NgModule({
  declarations: [PayInvoiceComponent],
  imports: [CommonModule, SharedModule, PaymentsRoutingModule]
})
export class PaymentsModule {}
