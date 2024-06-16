import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

export const adminProfesorGuard: CanActivateFn = (route, state) => {
  const authservice = inject(AuthService)
  const router = inject(Router)

  // Si el token no es admin o profesor nos manda al login
  return authservice.getUserData().rol = "admin" || "profesor"? true : router.navigateByUrl("/login");

};
