import { Component } from '@angular/core';
import { TareaDto } from '../../interfaces/tarea';
import { ClaseDto } from '../../interfaces/clase';
import { TareaService } from '../../services/tarea.service';
import { ClaseService } from '../../services/clase.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { faClipboard, faListCheck } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-calificaciones-de-clase',
  templateUrl: './calificaciones-de-clase.component.html',
  styleUrl: './calificaciones-de-clase.component.css'
})
export class CalificacionesDeClaseComponent {
  tareas: TareaDto[] = [];
  clase: ClaseDto | undefined;
  id: number = 0;
  p: number = 1;
  faListCheck = faListCheck;


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
  faClipboard = faClipboard
}
