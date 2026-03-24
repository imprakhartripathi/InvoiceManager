import { Component, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { TemplateField } from '../../../../models/template-field.model';
import { TemplateDto, TemplateService } from '../../../templates/template.service';
import { InvoiceService } from '../../invoice.service';

@Component({
  standalone: false,
  selector: 'app-invoice-create',
  templateUrl: './invoice-create.component.html',
  styleUrls: ['./invoice-create.component.scss']
})
export class InvoiceCreateComponent implements OnInit {
  readonly templates = signal<TemplateDto[]>([]);
  readonly selectedTemplate = signal<TemplateDto | null>(null);
  readonly form;
  readonly calculatedTotal = signal(0);
  readonly loadingTemplates = signal(true);
  submitted = false;
  isSubmitting = false;
  message = '';
  error = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly templateService: TemplateService,
    private readonly invoiceService: InvoiceService
  ) {
    this.form = this.fb.group({
      templateId: ['', Validators.required],
      data: this.fb.group({}),
      lineItems: this.fb.array<FormGroup>([])
    });
  }

  ngOnInit(): void {
    this.templateService.list().subscribe({
      next: (templates) => {
        this.loadingTemplates.set(false);
        this.templates.set(templates);
        const routeTemplateId = this.route.snapshot.paramMap.get('templateId');
        const selectedId =
          routeTemplateId && templates.some((t) => t.id === routeTemplateId) ? routeTemplateId : templates[0]?.id ?? '';
        if (selectedId) this.onTemplateChange(selectedId);
      },
      error: () => {
        this.loadingTemplates.set(false);
        this.error = 'Unable to load templates.';
      }
    });

    this.lineItems.valueChanges.subscribe(() => this.recalculateTotal());
  }

  get dataGroup(): FormGroup {
    return this.form.get('data') as FormGroup;
  }

  get lineItems(): FormArray<FormGroup> {
    return this.form.get('lineItems') as FormArray<FormGroup>;
  }

  onTemplateChange(templateId: string): void {
    this.form.patchValue({ templateId });
    const template = this.templates().find((t) => t.id === templateId) ?? null;
    this.selectedTemplate.set(template);

    this.resetDataForm(template?.fields ?? []);

    this.lineItems.clear();
    if (template?.hasLineItems) {
      this.addLineItem();
    }
  }

  addLineItem(): void {
    this.lineItems.push(
      this.fb.group({
        description: [''],
        quantity: [1, Validators.required],
        unitPrice: [0, Validators.required]
      })
    );
    this.recalculateTotal();
  }

  submit(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.error = 'Please complete all required fields.';
      return;
    }

    this.error = '';
    this.isSubmitting = true;
    this.invoiceService.create(this.form.getRawValue() as never).subscribe({
      next: (invoice) => {
        this.isSubmitting = false;
        this.message = `Invoice ${invoice.id} created successfully.`;
        this.router.navigateByUrl('/invoices');
      },
      error: () => {
        this.isSubmitting = false;
        this.error = 'Failed to create invoice. Please try again.';
      }
    });
  }

  private resetDataForm(fields: TemplateField[]): void {
    const controls: Record<string, FormControl> = {};
    fields.forEach((field) => {
      controls[field.key] = new FormControl(field.defaultValue ?? '', field.required ? Validators.required : []);
    });
    this.form.setControl('data', this.fb.group(controls));
    this.recalculateTotal();
  }

  private recalculateTotal(): void {
    const total = this.lineItems.controls.reduce((sum, group) => {
      const quantity = Number(group.get('quantity')?.value ?? 0);
      const unitPrice = Number(group.get('unitPrice')?.value ?? 0);
      return sum + quantity * unitPrice;
    }, 0);
    this.calculatedTotal.set(total);
  }
}
