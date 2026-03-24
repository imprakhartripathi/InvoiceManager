import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';
import { InvoiceDetailsDialogComponent } from './components/invoice-details-dialog/invoice-details-dialog.component';
import { InvoiceCreateComponent } from './pages/invoice-create/invoice-create.component';
import { InvoiceListComponent } from './pages/invoice-list/invoice-list.component';
import { InvoicesRoutingModule } from './invoices-routing.module';

@NgModule({
  declarations: [InvoiceListComponent, InvoiceCreateComponent, InvoiceDetailsDialogComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, InvoicesRoutingModule]
})
export class InvoicesModule {}
