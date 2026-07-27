import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

/**
 Se non sei loggato, vai a /login
 Se sei admin, entri
 Se sei loggato ma sei ROLE_USER vieni mandato a /books

 Questo protegge solo la UX fronend, la protezione vera resta lato BE con @PreAuthorize*/
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.isAdmin()) {
    return true;
  }

  return router.createUrlTree(['/books']);
};
