import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { SettingsService, UserSmtpSettingsRequest } from '../../settings.service';

@Component({
  standalone: false,
  selector: 'app-smtp-settings',
  templateUrl: './smtp-settings.component.html',
  styleUrls: ['./smtp-settings.component.scss']
})
export class SmtpSettingsComponent implements OnInit {
  readonly form;

  loading = false;
  saving = false;
  submitted = false;
  error = '';
  success = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly settingsService: SettingsService
  ) {
    this.form = this.fb.group({
      host: ['smtp.gmail.com'],
      port: [587, [Validators.min(1), Validators.max(65535)]],
      email: ['', [Validators.email]],
      appPassword: [''],
      enabled: [false]
    });
  }

  ngOnInit(): void {
    this.form.get('enabled')?.valueChanges.subscribe(() => this.applyConditionalValidators());
    this.load();
  }

  load(): void {
    this.loading = true;
    this.settingsService.getSmtpSettings().subscribe({
      next: (settings) => {
        this.loading = false;
        this.form.patchValue({
          host: settings.host,
          port: settings.port,
          email: settings.email,
          enabled: settings.enabled
        });
        this.applyConditionalValidators();
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  submit(): void {
    this.submitted = true;
    this.error = '';
    this.success = '';

    if (this.form.invalid) {
      this.error = 'Please fill all required SMTP fields.';
      return;
    }

    this.saving = true;
    const payload = this.form.getRawValue() as UserSmtpSettingsRequest;
    this.settingsService.upsertSmtpSettings(payload).subscribe({
      next: () => {
        this.saving = false;
        this.success = 'SMTP settings saved successfully.';
        this.form.patchValue({ appPassword: '' });
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message ?? 'Failed to save settings.';
      }
    });
  }

  private applyConditionalValidators(): void {
    const enabled = !!this.form.get('enabled')?.value;
    const hostCtrl = this.form.get('host');
    const portCtrl = this.form.get('port');
    const emailCtrl = this.form.get('email');
    const passCtrl = this.form.get('appPassword');

    if (enabled) {
      hostCtrl?.setValidators([Validators.required]);
      portCtrl?.setValidators([Validators.required, Validators.min(1), Validators.max(65535)]);
      emailCtrl?.setValidators([Validators.required, Validators.email]);
      passCtrl?.setValidators([Validators.required]);
    } else {
      hostCtrl?.clearValidators();
      portCtrl?.setValidators([Validators.min(1), Validators.max(65535)]);
      emailCtrl?.setValidators([Validators.email]);
      passCtrl?.clearValidators();
    }

    hostCtrl?.updateValueAndValidity();
    portCtrl?.updateValueAndValidity();
    emailCtrl?.updateValueAndValidity();
    passCtrl?.updateValueAndValidity();
  }
}
