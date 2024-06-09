import { Component, Input } from '@angular/core';
import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ClaseDto } from '../../interfaces/clase';
import { ClaseService } from '../../services/clase.service';
import { ActivatedRoute, Route, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';


@Component({
  selector: 'app-vista-de-clase',
  templateUrl: './vista-de-clase.component.html',
  styleUrls: ['./vista-de-clase.component.css']
})
export class VistaDeClaseComponent {
  tareas: TareaDto[] = [];
  clase: ClaseDto | undefined;
  id: number = 0;
  p: number = 1;

  constructor(
    private tareaService: TareaService,
    private claseService: ClaseService,
    private route: ActivatedRoute,
    public auth: AuthService,
    private router:Router
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
    this.tareaService.obtenerTareasPorClase(this.id).subscribe(
      (tareas) => {
        console.log('Tareas obtenidas:', tareas);
        this.tareas = tareas;
      },
      (error) => {
        console.error('Error al obtener las tareas:', error);
      }
    );
  }

  editarTarea(idTarea: number): void {
    this.router.navigate(['editar-tarea', idTarea]); 

  }

  eliminarTarea(idTarea: number): void {
    // Implementa la lógica para eliminar la tarea
  }
}
