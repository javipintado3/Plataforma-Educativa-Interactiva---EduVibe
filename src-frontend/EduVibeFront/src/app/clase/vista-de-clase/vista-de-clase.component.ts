import { Component, Input } from '@angular/core';
import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ClaseDto } from '../../interfaces/clase';
import { ClaseService } from '../../services/clase.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-vista-de-clase',
  templateUrl: './vista-de-clase.component.html',
  styleUrls: ['./vista-de-clase.component.css']
})
export class VistaDeClaseComponent {
  tareas: TareaDto[] = [];
  clase: ClaseDto | undefined;
  id: number = 0;

  constructor(
    private tareaService: TareaService,
    private claseService: ClaseService,
    private route: ActivatedRoute
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
}
