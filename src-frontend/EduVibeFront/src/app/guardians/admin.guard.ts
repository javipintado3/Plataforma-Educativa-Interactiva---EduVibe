import { CanActivateFn } from '@angular/router';
import { crearGuardDeRol } from './rol.guard';

// Si el token no es admin nos manda al login
export const adminGuard: CanActivateFn = crearGuardDeRol('admin');
