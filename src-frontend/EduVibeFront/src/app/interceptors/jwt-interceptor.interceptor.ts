import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { finalize } from 'rxjs';

/**
 * Endpoints que no requieren sesión. Las peticiones a estas rutas no deben
 * disparar el aviso de expiración ni el cierre de sesión automático.
 *
 * Antes esto se comprobaba con fragmentos sueltos y "/registeruser" no casaba
 * con "/registro", así que al crear una cuenta el interceptor llamaba a
 * logout() y echaba al usuario al login en mitad del registro.
 */
const RUTAS_PUBLICAS = ['/loginuser', '/registeruser', '/user/existeEmail', '/clases/todos'];

const esRutaPublica = (url: string): boolean =>
  RUTAS_PUBLICAS.some(ruta => url.includes(ruta));

/** Rutas para las que no merece la pena mostrar el indicador de carga. */
const SIN_INDICADOR_DE_CARGA = ['/user/existeEmail', '/clases'];

export const jwtInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService)
  const router = inject(Router)
  const loader = inject(NgxUiLoaderService)
  // getToken() comprueba que localStorage exista: en SSR no está definido y
  // acceder a él directamente rompía el renderizado en servidor.
  const token = authService.getToken() || "";

  const muestraCarga = !SIN_INDICADOR_DE_CARGA.some(ruta => req.url.includes(ruta));
  if (muestraCarga) {
    loader.start()
  }

  // Solo avisamos de la expiración en peticiones que sí necesitan sesión.
  if (!esRutaPublica(req.url) && authService.minutosRestantes() < 5) {

    if (authService.minutosRestantes() < 1) {
      authService.logout({ avisar: false })
      Swal.fire({
        title: "Sesión caducada",
        text: "Vuelve a iniciar sesión para continuar",
        icon: "warning"
      })
    } else {
      //redondeamos el valor de los minutos
      let minutos = Math.round(authService.minutosRestantes());
      //creamos una alerta
      Swal.fire({
        title: "Tu sesion va a expirar pronto",
        text: `A tu sesion le quedan menos de ${minutos} minuto/s por favor inicie sesion de nuevo`,
        icon: "warning",
        iconColor: "#ff6d43",
        confirmButtonText: "Renovar la sesion",
        confirmButtonColor: "#000000",
        showCancelButton: true,
        cancelButtonText: "Seguir navegando",
        cancelButtonColor: "#CF3F40"
      }).then(resp => {
        //si confirma pues mandamos a renovar
        if (resp.isConfirmed) {
          router.navigateByUrl("/renew")
        }
      })
    }
  }

  if (token) {
    req = req.clone({
      setHeaders: { "Authorization": token }
    })
  }

  return next(req).pipe(finalize(() => {
    if (muestraCarga) {
      loader.stop()
    }
  }));
};
