import { Component, OnInit } from '@angular/core';
import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ClaseDto } from '../../interfaces/clase';
import { ClaseService } from '../../services/clase.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-vista-de-clase',
  templateUrl: './vista-de-clase.component.html',
  styleUrls: ['./vista-de-clase.component.css']
})
export class VistaDeClaseComponent implements OnInit {
  tareas: TareaDto[] = [];
  clase: ClaseDto | undefined;
  id: number = 0;
  p: number = 1;

  constructor(
    private tareaService: TareaService,
    private claseService: ClaseService,
    private route: ActivatedRoute,
    public auth: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = +params['id']; // Convierte el parámetro a número
      this.obtenerClaseYtareas(); // Llama a la función para obtener la clase y las tareas
    });
  }

  obtenerClaseYtareas(): void {
    this.claseService.obtenerClasePorId(this.id).subscribe(
      (clase) => {
        console.log('Clase obtenida:', this.id);
        this.clase = clase;
        this.obtenerTareasPorClase();
      },
      (error) => {
        console.error('Error al obtener la clase:', error);
      }
    );
  }

  obtenerTareasPorClase(): void {
    if (this.auth.ifProfesor() || this.auth.ifAdmin()) {
      this.tareaService.obtenerTareasPorClase(this.id).subscribe(
        (tareas) => {
          console.log('Tareas obtenidas:', tareas);
          this.tareas = tareas;
        },
        (error) => {
          console.error('Error al obtener las tareas:', error);
        }
      );
    } else {
      this.tareaService.obtenerTareasPorClaseParaUsuarioActual(this.id).subscribe(
        (tareas) => {
          console.log('Tareas obtenidas:', tareas);
          this.tareas = tareas;
        },
        (error) => {
          console.error('Error al obtener las tareas:', error);
        }
      );
    }
  }

  editarTarea(idTarea: number): void {
    this.router.navigate(['editar-tarea', idTarea]); 
  }

  eliminarTarea(idTarea: number): void {
    Swal.fire({
      title: '¿Estás seguro?',
      text: 'No podrás revertir esto',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, eliminarla'
    }).then((result) => {
      if (result.isConfirmed) {
        this.tareaService.eliminarTarea(idTarea).subscribe(
          () => {
            Swal.fire(
              'Eliminada',
              'La tarea ha sido eliminada.',
              'success'
            );
            this.obtenerTareasPorClase(); // Recargar la lista de tareas después de eliminar
          },
          (error) => {
            Swal.fire(
              'Error',
              'Hubo un problema al eliminar la tarea.',
              'error'
            );
            console.error('Error al eliminar la tarea:', error);
          }
        );
      }
    });
  }
}
