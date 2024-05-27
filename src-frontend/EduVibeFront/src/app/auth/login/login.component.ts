import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
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
      next:resp=>{
        if(resp){
          Swal.fire({
            title: "Correcto",
            text: "Bienvenido",
            icon: "success",
            showConfirmButton:false
          });
          this.router.navigateByUrl("/dashboard")
        }else{
          Swal.fire({
            title: "Error",
            text: "Credenciales incorrectas",
            icon: "warning"
          });
        }
      },
      error:err=>{
        Swal.fire({
          title: "Error",
          text: "Usuario y/o contraseña incorrectos",
          icon: "error",
          showConfirmButton:false
        });
      }
    })
  }
}
