import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../../core/auth/auth.service';
import { LoggerService } from '../../../../core/services/logger.service';

@Component({
  standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  readonly form;

  error = '';
  submitted = false;
  isSubmitting = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly logger: LoggerService
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  submit(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      if (this.form.controls.email.invalid) {
        this.error = 'Please enter a valid email address.';
      } else if (this.form.controls.password.invalid) {
        this.error = 'Password is required.';
      } else {
        this.error = 'Please fix the form and try again.';
      }
      return;
    }

    this.isSubmitting = true;
    this.authService.login(this.form.getRawValue() as { email: string; password: string }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigateByUrl('/dashboard');
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.logger.error('Login failed', err);
        this.error = err?.error?.message ?? err?.message ?? 'Invalid email or password';
      }
    });
  }
}
