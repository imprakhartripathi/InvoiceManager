import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../../core/auth/auth.service';
import { LoggerService } from '../../../../core/services/logger.service';

@Component({
  standalone: false,
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent {
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
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  submit(): void {
    this.submitted = true;
    this.error = '';

    if (this.form.invalid) {
      const emailControl = this.form.controls.email;
      const passwordControl = this.form.controls.password;

      if (emailControl.invalid) {
        this.error = 'Please enter a valid email address.';
      } else if (passwordControl.invalid) {
        this.error = 'Password must be at least 8 characters.';
      } else {
        this.error = 'Please fix the form errors and try again.';
      }

      this.logger.warn('Signup blocked due to invalid form', {
        emailInvalid: emailControl.invalid,
        passwordInvalid: passwordControl.invalid
      });
      return;
    }

    this.isSubmitting = true;
    this.authService.signup(this.form.getRawValue() as { email: string; password: string }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.logger.info('Signup success. Redirecting to login.');
        this.router.navigateByUrl('/auth/login');
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.logger.error('Signup failed', err);
        this.error = err?.error?.message ?? err?.message ?? 'Signup failed';
      }
    });
  }
}
