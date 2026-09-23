import { CanActivateFn } from '@angular/router';
import { crearGuardDeRol } from './rol.guard';

// Si el token no es alumno nos manda al login
export const alumnoGuard: CanActivateFn = crearGuardDeRol('alumno');
