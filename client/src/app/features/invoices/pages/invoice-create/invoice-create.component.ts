import { Component, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { TemplateField } from '../../../../models/template-field.model';
import { UserProfile, UserProfileService } from '../../../../core/services/user-profile.service';
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
  readonly profile = signal<UserProfile | null>(null);
  readonly displayNameOptions = signal<string[]>([]);
  readonly form;
  readonly calculatedTotal = signal(0);
  readonly loadingTemplates = signal(true);
  readonly editInvoiceId = signal<string | null>(null);
  readonly isEditMode = signal(false);
  submitted = false;
  isSubmitting = false;
  message = '';
  error = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly templateService: TemplateService,
    private readonly invoiceService: InvoiceService,
    private readonly userProfileService: UserProfileService
  ) {
    this.form = this.fb.group({
      templateId: ['', Validators.required],
      displayName: ['', Validators.required],
      customDisplayName: [''],
      data: this.fb.group({}),
      lineItems: this.fb.array<FormGroup>([])
    });
  }

  ngOnInit(): void {
    const invoiceId = this.route.snapshot.paramMap.get('invoiceId');
    this.editInvoiceId.set(invoiceId);
    this.isEditMode.set(!!invoiceId);

    forkJoin({
      templates: this.templateService.list(),
      profile: this.userProfileService.getMe()
    }).subscribe({
      next: ({ templates, profile }) => {
        this.loadingTemplates.set(false);
        this.templates.set(templates);
        this.profile.set(profile);

        const options = [profile.defaultDisplayName, ...(profile.savedCustomDisplayNames ?? [])]
          .map((name) => name?.trim())
          .filter((name): name is string => !!name);
        this.displayNameOptions.set(Array.from(new Set(options)));
        this.form.patchValue({ displayName: profile.defaultDisplayName });

        if (invoiceId) {
          this.loadForEdit(invoiceId);
          return;
        }

        const routeTemplateId = this.route.snapshot.paramMap.get('templateId');
        const selectedId =
          routeTemplateId && templates.some((t) => t.id === routeTemplateId) ? routeTemplateId : templates[0]?.id ?? '';
        if (selectedId) this.onTemplateChange(selectedId);
      },
      error: () => {
        this.loadingTemplates.set(false);
        this.error = 'Unable to load templates or profile data.';
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

    const customDisplayName = String(this.form.get('customDisplayName')?.value ?? '').trim();
    let selectedDisplayName = String(this.form.get('displayName')?.value ?? '').trim();
    if (!selectedDisplayName && customDisplayName) {
      selectedDisplayName = customDisplayName;
    }
    if (!selectedDisplayName) {
      this.error = 'Please choose a display name for the invoice.';
      return;
    }

    this.error = '';
    this.isSubmitting = true;
    const payload = {
      templateId: String(this.form.get('templateId')?.value ?? ''),
      displayName: selectedDisplayName,
      data: this.dataGroup.getRawValue(),
      lineItems: this.lineItems.getRawValue()
    };

    const save$ = this.isEditMode() && this.editInvoiceId()
      ? this.invoiceService.update(this.editInvoiceId() as string, {
          displayName: payload.displayName,
          data: payload.data,
          lineItems: payload.lineItems
        })
      : this.invoiceService.create(payload);

    save$.subscribe({
      next: (invoice) => {
        this.persistCustomDisplayNameIfNeeded(customDisplayName);
        this.isSubmitting = false;
        this.message = `Invoice ${invoice.id} ${this.isEditMode() ? 'updated' : 'created'} successfully.`;
        this.router.navigateByUrl('/invoices');
      },
      error: (err) => {
        this.isSubmitting = false;
        this.error = err?.error?.message ?? 'Failed to save invoice. Please try again.';
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

  private loadForEdit(invoiceId: string): void {
    this.invoiceService.getOne(invoiceId).subscribe({
      next: (invoice) => {
        this.onTemplateChange(invoice.templateId);
        this.form.patchValue({
          templateId: invoice.templateId,
          displayName: invoice.displayName
        });

        setTimeout(() => {
          this.dataGroup.patchValue(invoice.data ?? {});
          this.lineItems.clear();
          (invoice.lineItems ?? []).forEach((item) => {
            this.lineItems.push(
              this.fb.group({
                description: [item['description'] ?? ''],
                quantity: [item['quantity'] ?? 1, Validators.required],
                unitPrice: [item['unitPrice'] ?? 0, Validators.required]
              })
            );
          });
          this.recalculateTotal();
        }, 0);
      },
      error: () => {
        this.error = 'Unable to load invoice for editing.';
      }
    });
  }

  private persistCustomDisplayNameIfNeeded(customDisplayName: string): void {
    if (!customDisplayName || !this.profile()) {
      return;
    }
    const profile = this.profile() as UserProfile;
    this.userProfileService
      .updateMe({
        fullName: profile.fullName,
        businessName: profile.businessName,
        defaultDisplayName: profile.defaultDisplayName,
        newCustomDisplayName: customDisplayName
      })
      .subscribe();
  }
}
