import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PayInvoiceComponent } from './pages/pay-invoice/pay-invoice.component';

const routes: Routes = [{ path: ':invoiceId', component: PayInvoiceComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PaymentsRoutingModule {}
