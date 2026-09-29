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

/**
 * Fabrica un guard que exige sesión y, además, que la persona cumpla el rol.
 *
 * Los tres guards por rol eran el mismo código con una pregunta distinta, así
 * que se comparte el cuerpo y cada uno solo dice cuál es su pregunta. Quien no
 * cumple vuelve a /clases, que existe para todos los roles.
 */
const exigirRol = (cumple: (auth: AuthService) => boolean): CanActivateFn => (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.estaAutenticado()) {
    return router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });
  }

  return cumple(auth) ? true : router.createUrlTree(['/clases']);
};

/** Exige además rol de administración. */
export const adminGuard = exigirRol(auth => auth.esAdmin());

/** Exige además rol de profesorado. */
export const profesorGuard = exigirRol(auth => auth.esProfesor());

/** Exige además rol de alumnado. */
export const alumnoGuard = exigirRol(auth => auth.esAlumno());

/** Impide volver al login teniendo ya la sesión abierta. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.estaAutenticado() ? router.createUrlTree(['/clases']) : true;
};
