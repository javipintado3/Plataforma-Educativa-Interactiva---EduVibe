import { Component, OnInit } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { ClaseDto } from '../../interfaces/clase';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';

@Component({
  selector: 'app-mis-clases',
  templateUrl: './mis-clases.component.html',
  styleUrls: ['./mis-clases.component.css']
})
export class MisClasesComponent implements OnInit {
  clases: ClaseDto[] = [];
  todasLasClases: ClaseDto[] = [];
  p: number = 1;
  searchText: string = '';

  constructor(
    private claseService: ClaseService,
    public auth: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Verificar si el usuario es admin
    if (this.auth.ifAdmin()) {
      this.obtenerTodasLasClases();
    } else {
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
  }

  obtenerTodasLasClases(): void {
    this.claseService.obtenerTodasLasClases().subscribe(
      clases => {
        this.clases = clases;
        this.todasLasClases = clases; // Guardamos una copia de todas las clases
        this.ordenarClases();
      },
      error => {
        console.error('Error al obtener todas las clases:', error);
      }
    );
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

  ordenarClases(): void {
    this.clases.sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  buscarClase(): void {
    if (this.searchText.trim() === '') {
      // Si el campo de búsqueda está vacío, mostramos todas las clases
      this.clases = this.todasLasClases;
    } else {
      // Si hay texto en el campo de búsqueda, filtramos las clases
      const searchText = this.searchText.toLowerCase();
      this.clases = this.todasLasClases.filter(clase => clase.nombre.toLowerCase().includes(searchText));
    }
  }

  editarClase(idClase: number): void {
    this.router.navigate(['editar-clase', idClase]);
  }

  eliminarClase(idClase: number): void {
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
        this.claseService.eliminarClase(idClase).subscribe(
          () => {
            Swal.fire(
              '¡Eliminado!',
              'La clase ha sido eliminada correctamente.',
              'success'
            );
            this.obtenerTodasLasClases(); // Vuelve a cargar las clases después de eliminar
          },
          error => {
            console.error('Error al eliminar la clase:', error);
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
