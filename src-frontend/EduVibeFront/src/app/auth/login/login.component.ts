import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { LoginRequest } from '../../request/loginRequest';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  title:string = "Inicio Sesión";
  constructor(private authService:AuthService,
    private router:Router
  ){}

  loginRequest:LoginRequest={
    email:"",
    password:""
  }
  login(){
    this.authService.login(this.loginRequest)
    .subscribe({
      next:()=>{
        Swal.fire({
          title: "Correcto",
          text: "Bienvenido",
          icon: "success",
          showConfirmButton:false,
          timer: 1200
        });
        this.router.navigateByUrl("/inicio")
      },
      error:(err: HttpErrorResponse)=>{
        Swal.fire({
          title: "Error",
          text: this.mensajeDeError(err),
          icon: "error"
        });
      }
    })
  }

  /**
   * Antes cualquier fallo se mostraba como "usuario y/o contraseña incorrectos",
   * así que un servidor caído o un problema de CORS parecían un error de
   * credenciales y no había forma de distinguirlos.
   */
  private mensajeDeError(err: HttpErrorResponse): string {
    if (err.status === 401) {
      return "Usuario y/o contraseña incorrectos";
    }
    if (err.status === 0) {
      return "No se ha podido contactar con el servidor. Comprueba que la API está levantada.";
    }
    return err.error?.message || `Error inesperado del servidor (${err.status})`;
  }
}
