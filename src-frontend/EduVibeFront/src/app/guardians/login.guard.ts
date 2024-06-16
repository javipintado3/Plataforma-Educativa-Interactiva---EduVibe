import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { tap } from 'rxjs';

export const loginGuard: CanMatchFn = (route, segments) => {
  const token:string = localStorage.getItem("token") || "";
  const authService = inject(AuthService)
  const router = inject(Router)
//Si el token no es valido por:



//  token malformado, rol cambiado o token expirado; nos manda al login.
  return authService.getUserData().rol = "alumno" || "admin" || "profesor"? true : router.navigateByUrl("/login");
};
