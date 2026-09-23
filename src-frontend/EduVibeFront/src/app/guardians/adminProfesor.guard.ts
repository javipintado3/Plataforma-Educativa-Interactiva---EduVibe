import { CanActivateFn } from '@angular/router';
import { crearGuardDeRol } from './rol.guard';

// Si el token no es admin o profesor nos manda al login
export const adminProfesorGuard: CanActivateFn = crearGuardDeRol('admin', 'profesor');
