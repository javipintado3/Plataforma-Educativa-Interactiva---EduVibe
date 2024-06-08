import { Component } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { ClaseDto } from '../../interfaces/clase';
import { AuthService } from '../../services/auth.service';

import Swal from 'sweetalert2';

import { Router } from '@angular/router';

@Component({
  selector: 'app-mis-clases',
  templateUrl: './mis-clases.component.html',
  styleUrl: './mis-clases.component.css'
})
export class MisClasesComponent {
  clases: ClaseDto[] = [];
  p: number = 1;

  constructor(
    private claseService: ClaseService,
    public auth: AuthService,
    private router:Router
  ) { }

  ngOnInit(): void {
    // Obtener el ID del usuario del token almacenado en el localStorage
    const id = this.auth.getUserId();

    // Verificar si el ID del usuario está disponible
    if (id) {
      // Llamar al servicio para obtener las clases del usuario
      this.obtenerClasesPorUsuario(id);
    } else {
      console.error('No se pudo obtener el ID del usuario.');
    }
  }

  obtenerClasesPorUsuario(id: number): void {
    // Llamar al servicio para obtener las clases del usuario por su ID
    this.claseService.obtenerClasesPorUsuario(id).subscribe(
      clases => {
        this.clases = clases;
      },
      error => {
        console.error('Error al obtener las clases del usuario:', error);
      }
    );
  }

  editarClase(idClase: number): void {
    this.router.navigate(['editar-clase', idClase]); // Sin el prefijo ':'
  }
  

  eliminarClase(idClase: number): void {
    // Mostrar SweetAlert de confirmación antes de eliminar la clase
    Swal.fire({
      title: '¿Estás seguro?',
      text: 'Una vez eliminada, no podrás recuperar esta clase.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, eliminar'
    }).then((result) => {
      if (result.isConfirmed) {
        // Llamar al servicio para eliminar la clase
        this.claseService.eliminarClase(idClase).subscribe(
          () => {
            // Si la eliminación es exitosa, mostrar SweetAlert de éxito
            Swal.fire(
              '¡Eliminado!',
              'La clase ha sido eliminada correctamente.',
              'success'
            );
          },
          error => {
            console.error('Error al eliminar la clase:', error);
            // Mostrar SweetAlert de error si hay algún problema al eliminar la clase
            Swal.fire(
              '¡Error!',
              'Ocurrió un error al intentar eliminar la clase.',
              'error'
            );
          }
        );
      }
    });
  }
  
 }
