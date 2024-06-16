import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { finalize } from 'rxjs';

export const jwtInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem("token") || "";
  const authService = inject(AuthService)
  const router = inject(Router)
  const loader = inject(NgxUiLoaderService)

  
  if(!req.url.includes("/user/existeEmail")
  && !req.url.includes("/clases")
) {
  
  loader.start()
  }
  //si los minutos restantes son menor a 10 y en la url no está siendo una peticion para login entra
  if(authService.minutosRestantes()<5 
  && !req.url.includes("/login")
  && !req.url.includes("/user/existeEmail")
  && !req.url.includes("/registro")
  && !req.url.includes("/clases/todos")
) {
  
  if(authService.minutosRestantes()<1 && !req.url.includes("/registro")){
    authService.logout()
  }else{

    //redondeamos el valor de los minutos
    let minutos = Math.round(authService.minutosRestantes());
    //creamos una alerta
    Swal.fire({
      title:"Tu sesion va a expirar pronto",
      text:`${minutos>0? `A tu sesion le quedan menos de ${minutos} minuto/s por favor inicie sesion de nuevo`:'Sesion caducada'} `,
      icon: "warning",
      iconColor:"#ff6d43",
      confirmButtonText:"Renovar la sesion",
      confirmButtonColor:"#000000",
      showCancelButton:true,
      cancelButtonText:"Seguir navegando",
      cancelButtonColor: "#CF3F40"
    }).then(resp=>{
      //si confirma pues mandamos al login
      if(resp.isConfirmed){
        console.log("entra")
        router.navigateByUrl("/renew")
      }
    })
  }
  }
  if(token){
    req = req.clone({
      setHeaders: {"Authorization": token}
    }
  )}
  
  return next(req).pipe(finalize(()=> loader.stop()));
};
