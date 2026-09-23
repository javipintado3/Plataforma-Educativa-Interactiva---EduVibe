import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Exige sesión abierta.
 *
 * Devuelve un UrlTree en lugar de llamar a navigateByUrl: es la forma correcta
 * de redirigir desde un guard, porque no lanza una navegación mientras la
 * anterior sigue en curso.
 */
export const sesionGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estaAutenticado()) {
    return true;
  }

  // Se recuerda a dónde iba para volver ahí después de entrar
  return router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });
};

/** Exige además rol de administración. */
export const adminGuard: CanActivateFn = (ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.estaAutenticado()) {
    return router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });
  }

  return auth.esAdmin() ? true : router.createUrlTree(['/clases']);
};

/** Impide volver al login teniendo ya la sesión abierta. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.estaAutenticado() ? router.createUrlTree(['/clases']) : true;
};
