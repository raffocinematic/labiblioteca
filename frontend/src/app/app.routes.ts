import { Routes } from '@angular/router';

import { AppShellComponent } from './layout/app-shell/app-shell.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent)
      },
      {
        path: 'books',
        loadComponent: () => import('./features/books/book-list.component').then((m) => m.BookListComponent)
      },
      {
        path: 'admin/users',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/users/admin-users.component').then((m) => m.AdminUsersComponent)
      },
      {
        path: 'admin/reports',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/reports/admin-reports.component').then((m) => m.AdminReportsComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
