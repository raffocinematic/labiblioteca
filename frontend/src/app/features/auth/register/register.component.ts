import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ApiError } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly registerForm;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.registerForm = this.formBuilder.nonNullable.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  protected register(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const request = this.registerForm.getRawValue();

    if (request.password !== request.confirmPassword) {
      this.error.set('Password e conferma password non coincidono.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authService.register(request).subscribe({
      next: () => {
        this.router.navigateByUrl('/books');
      },
      error: (error: HttpErrorResponse) => {
        const apiError = error.error as ApiError | null;
        this.error.set(apiError?.message ?? 'Registrazione non riuscita.');
        this.loading.set(false);
      }
    });
  }
}
