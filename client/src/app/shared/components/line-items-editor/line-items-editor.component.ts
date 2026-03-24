import { Component, Input } from '@angular/core';
import { FormArray, FormGroup } from '@angular/forms';

@Component({
  standalone: false,
  selector: 'app-line-items-editor',
  templateUrl: './line-items-editor.component.html',
  styleUrls: ['./line-items-editor.component.scss']
})
export class LineItemsEditorComponent {
  @Input({ required: true }) lineItems!: FormArray<FormGroup>;

  removeLineItem(index: number): void {
    this.lineItems.removeAt(index);
  }
}
