import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Crea un guard que solo deja pasar si el rol del usuario está entre los
 * indicados. Si no hay sesión, o el rol no encaja, devuelve un UrlTree hacia
 * /login: es la forma correcta de redirigir desde un guard, en lugar de lanzar
 * una navegación con navigateByUrl mientras la anterior sigue en curso.
 */
export const crearGuardDeRol = (...rolesPermitidos: string[]): CanActivateFn => () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const rol = authService.getUserData()?.rol;

  return rol && rolesPermitidos.includes(rol) ? true : router.createUrlTree(['/login']);
};
