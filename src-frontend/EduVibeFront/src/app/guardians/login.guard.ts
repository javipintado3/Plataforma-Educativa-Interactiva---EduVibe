import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Deja pasar solo si hay una sesión abierta. El rol concreto lo comprueban los
 * guards de rol; aquí únicamente se exige estar autenticado.
 */
export const loginGuard: CanMatchFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLogged() ? true : router.createUrlTree(['/login']);
};
