import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { InvoiceCreateComponent } from './pages/invoice-create/invoice-create.component';
import { InvoiceListComponent } from './pages/invoice-list/invoice-list.component';

const routes: Routes = [
  { path: '', component: InvoiceListComponent },
  { path: 'new', component: InvoiceCreateComponent },
  { path: 'new/:templateId', component: InvoiceCreateComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvoicesRoutingModule {}
