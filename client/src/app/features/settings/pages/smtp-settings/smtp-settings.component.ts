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
      host: ['smtp.gmail.com', [Validators.required]],
      port: [587, [Validators.required, Validators.min(1), Validators.max(65535)]],
      email: ['', [Validators.required, Validators.email]],
      appPassword: ['', [Validators.required]],
      enabled: [true]
    });
  }

  ngOnInit(): void {
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
}
