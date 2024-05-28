import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

export const adminGuard: CanActivateFn = (route, state) => {
  const authservice = inject(AuthService)
  const router = inject(Router)

  // Si el token no es admin nos manda al login
  return authservice.getUserData().role=="admin"? true : router.navigateByUrl("/login");

};
