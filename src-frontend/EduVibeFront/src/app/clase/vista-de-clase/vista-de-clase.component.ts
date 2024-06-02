import { Component } from '@angular/core';
import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ClaseDto } from '../../interfaces/clase';
import { ClaseService } from '../../services/clase.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-vista-de-clase',
  templateUrl: './vista-de-clase.component.html',
  styleUrl: './vista-de-clase.component.css'
})
export class VistaDeClaseComponent {
  tareas: TareaDto[] = [];
  clase: ClaseDto | undefined;

  constructor(
    private tareaService: TareaService,
    private claseService: ClaseService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const idClase = parseInt(params['idClase'], 10); // Convertir a número
      console.log('ID de la clase:', idClase);
      if (!isNaN(idClase)) {
        this.obtenerClaseYtareas(idClase);
      } else {
        console.error('ID de la clase no es un número válido:', params['idClase']);
      }
    });
  }

  obtenerClaseYtareas(idClase: number): void {
    this.claseService.obtenerClasePorId(idClase).subscribe(
      (clase) => {
        console.log('Clase obtenida:', clase);
        this.clase = clase;
        this.obtenerTareasPorClase(idClase);
      },
      (error) => {
        console.error('Error al obtener la clase:', error);
      }
    );
  }

  obtenerTareasPorClase(idClase: number): void {
    this.tareaService.obtenerTareasPorClase(idClase).subscribe(
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
