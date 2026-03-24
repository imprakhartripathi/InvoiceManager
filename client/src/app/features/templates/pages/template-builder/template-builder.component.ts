import { Component } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { TemplateService } from '../../template.service';

@Component({
  standalone: false,
  selector: 'app-template-builder',
  templateUrl: './template-builder.component.html',
  styleUrls: ['./template-builder.component.scss']
})
export class TemplateBuilderComponent {
  readonly form;
  error = '';
  isSubmitting = false;
  submitted = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly templateService: TemplateService,
    private readonly router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      hasLineItems: [false],
      fields: this.fb.array([
        this.fb.group({
          key: ['customerName', Validators.required],
          label: ['Customer Name', Validators.required],
          type: ['text', Validators.required],
          required: [true],
          defaultValue: ['']
        }),
        this.fb.group({
          key: ['customerEmail', Validators.required],
          label: ['Customer Email', Validators.required],
          type: ['text', Validators.required],
          required: [true],
          defaultValue: ['']
        })
      ])
    });
  }

  get fields(): FormArray {
    return this.form.get('fields') as FormArray;
  }

  addField(): void {
    this.fields.push(
      this.fb.group({
        key: ['', Validators.required],
        label: ['', Validators.required],
        type: ['text', Validators.required],
        required: [false],
        defaultValue: ['']
      })
    );
  }

  removeField(index: number): void {
    if (this.fields.length <= 1) {
      return;
    }
    this.fields.removeAt(index);
  }

  submit(): void {
    this.submitted = true;

    if (this.form.invalid) {
      this.error = 'Please complete required fields.';
      return;
    }

    this.error = '';
    this.isSubmitting = true;
    this.templateService.create(this.form.getRawValue() as never).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigateByUrl('/templates');
      },
      error: () => {
        this.isSubmitting = false;
        this.error = 'Unable to save template. Please try again.';
      }
    });
  }
}
