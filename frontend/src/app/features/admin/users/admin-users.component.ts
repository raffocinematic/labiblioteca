import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { AppUser, UserRole } from '../../../core/models/user.model';
import { UserAdminApiService } from '../../../core/services/user-admin-api.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-admin-users',
  imports: [PageHeaderComponent],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  protected readonly users = signal<AppUser[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor(private readonly userAdminApiService: UserAdminApiService) {
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  protected loadUsers(): void {
    this.loading.set(true);
    this.error.set(null);

    this.userAdminApiService.getUsers().subscribe({
      next: (response) => {
        this.users.set(response.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossibile caricare gli utenti.');
        this.loading.set(false);
      }
    });
  }

  protected changeRole(user: AppUser, role: UserRole): void {
    if (user.role === role) {
      return;
    }

    this.userAdminApiService.updateUserRole(user.id, role).subscribe({
      next: () => this.loadUsers(),
      error: (error: unknown) => {
        this.error.set(this.getApiErrorMessage(error, 'Impossibile modificare il ruolo.'));
      }
    });
  }

  protected deleteUser(user: AppUser): void {
    const confirmed = window.confirm(`Vuoi eliminare l'utente "${user.username}"?`);

    if (!confirmed) {
      return;
    }

    this.userAdminApiService.deleteUser(user.id).subscribe({
      next: () => this.loadUsers(),
      error: (error: unknown) => {
        this.error.set(this.getApiErrorMessage(error, "Impossibile eliminare l'utente."));
      }
    });
  }

  protected formatCreatedAt(createdAt: string): string {
    return new Intl.DateTimeFormat('it-IT', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(createdAt));
  }

  private getApiErrorMessage(error: unknown, fallbackMessage: string): string {
    if (error instanceof HttpErrorResponse) {
      const message = error.error?.message;

      if (typeof message === 'string' && message.trim().length > 0) {
        return message;
      }
    }

    return fallbackMessage;
  }
}
