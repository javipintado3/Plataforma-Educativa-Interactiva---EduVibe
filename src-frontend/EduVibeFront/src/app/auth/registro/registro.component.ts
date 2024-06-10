import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import Swal from 'sweetalert2';
import { AuthService } from '../../services/auth.service';
import { ExisteCorreoService } from '../../validators/existe-correo.service';
import { ValidatorService } from '../../validators/validator.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-registro',
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent {
  constructor(private fb: FormBuilder, // Inyección del FormBuilder para construir formularios
  private validators: ValidatorService, // Inyección del servicio ValidatorService para validaciones personalizadas
  private emailTaken: ExisteCorreoService, // Inyección del servicio ExisteCorreoService para verificar la disponibilidad de un correo electrónico
  private authService: AuthService,
  private router: Router,
  
) { }


ngOnInit(): void {

}



// Definición del formulario utilizando FormBuilder

myForm: FormGroup = this.fb.group({
  email: ["", [Validators.required, Validators.pattern(/^[^\.\s][\w\-]+(\.[\w\-]+)*@([\w-]+\.)+[\w-]{2,}$/), this.validators.correoVibe]],
  nombre: ["", [Validators.required]],
  rol: [""],
  password: ["", [Validators.required, Validators.pattern(/^.{8,}$/)]]
});

// Objeto de usuario para almacenar datos
user = {
  nombre: '',
  email: '',
  password: '',
  rol: ''
};

// Función para verificar si un campo del formulario es válido
noValido(campo: string): boolean {
  return this.myForm?.controls[campo]?.invalid && this.myForm?.controls[campo]?.touched
}

// Función para obtener mensajes de error del campo de email
get emailError(): string {
  const errors = this.myForm.get('email')?.errors
  let msg: string = ""
  if (errors) {
    if (errors['required']) {
      msg = "El Email es necesario"
    } else if (errors["pattern"]) {
      msg = "Formato de email necesario (ejemplo@vibe.com)"
    } else if (errors["noVibeMail"]) {
      msg = "No es un correo de EduVibe"
    } else if (errors["emailTaken"]) {
      msg = "Este correo no está disponible"
    }
  }
  return msg;
}

// Función para obtener mensajes de error del campo de contraseña
get passwordError(): string {
  const errors = this.myForm.get('password')?.errors
  let msg: string = ""
  if (errors) {
    if (errors['required']) {
      msg = "La contraseña es necesaria"
    } else if (errors["pattern"]) {
      msg = "La contraseña tiene que tener 8 caracteres mínimo"
    }
  }
  return msg;
}



// Función para realizar el registro si el formulario es válido
register() {
  this.myForm.markAllAsTouched()
  if (this.myForm.valid) {
    if (this.myForm.valid) {
      console.log(this.myForm.value)
      this.authService.register(this.myForm.value)
        .subscribe({
          next: resp => {
            Swal.fire({
              title: "Usuario Registrado",
              icon: "success",
              showConfirmButton: false
            })
            this.router.navigateByUrl("/users")

          },
          error: err => {
            Swal.fire({
              title: "Error al crear usuario",
              icon: "error",
              showConfirmButton: false
            })
          }
        })
    }
  }
}


}
