import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';
import { InvoiceCreateComponent } from './pages/invoice-create/invoice-create.component';
import { InvoiceListComponent } from './pages/invoice-list/invoice-list.component';
import { InvoicesRoutingModule } from './invoices-routing.module';

@NgModule({
  declarations: [InvoiceListComponent, InvoiceCreateComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, InvoicesRoutingModule]
})
export class InvoicesModule {}
