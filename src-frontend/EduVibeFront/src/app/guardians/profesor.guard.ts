import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

export const profesorGuard: CanActivateFn = (route, state) => {
  const authservice = inject(AuthService)
  const router = inject(Router)

  // Si el token no es profesor nos manda al login
  return authservice.getUserData().rol=="profesor"? true : router.navigateByUrl("/login");

};
