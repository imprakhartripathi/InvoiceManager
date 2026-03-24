import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

import { InvoiceDto } from '../../invoice.service';

@Component({
  standalone: false,
  selector: 'app-invoice-details-dialog',
  templateUrl: './invoice-details-dialog.component.html',
  styleUrls: ['./invoice-details-dialog.component.scss']
})
export class InvoiceDetailsDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { invoice: InvoiceDto; templateName: string; previewFields: string[] }) {}
}
