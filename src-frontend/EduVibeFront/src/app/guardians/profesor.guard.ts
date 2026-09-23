import { CanActivateFn } from '@angular/router';
import { crearGuardDeRol } from './rol.guard';

// Si el token no es profesor nos manda al login
export const profesorGuard: CanActivateFn = crearGuardDeRol('profesor');
