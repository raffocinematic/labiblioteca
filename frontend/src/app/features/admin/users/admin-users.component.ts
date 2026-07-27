import { Component, OnInit, signal } from '@angular/core';

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
    this.userAdminApiService.updateUserRole(user.id, role).subscribe({
      next: () => this.loadUsers(),
      error: () => this.error.set('Impossibile modificare il ruolo.')
    });
  }

  protected deleteUser(user: AppUser): void {
    const confirmed = window.confirm(`Vuoi eliminare l'utente "${user.username}"?`);

    if (!confirmed) {
      return;
    }

    this.userAdminApiService.deleteUser(user.id).subscribe({
      next: () => this.loadUsers(),
      error: () => this.error.set("Impossibile eliminare l'utente.")
    });
  }
}
