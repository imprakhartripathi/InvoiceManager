import { Component, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { TemplateField } from '../../../models/template-field.model';

@Component({
  standalone: false,
  selector: 'app-dynamic-field',
  templateUrl: './dynamic-field.component.html',
  styleUrls: ['./dynamic-field.component.scss']
})
export class DynamicFieldComponent {
  @Input({ required: true }) field!: TemplateField;
  @Input({ required: true }) form!: FormGroup;
}
