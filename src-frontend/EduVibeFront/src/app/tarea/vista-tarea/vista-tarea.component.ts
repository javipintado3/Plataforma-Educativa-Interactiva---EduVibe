import { Component, Input } from '@angular/core';

import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-vista-tarea',
  templateUrl: './vista-tarea.component.html',
  styleUrl: './vista-tarea.component.css'
})
export class VistaTareaComponent {
  tarea: TareaDto | undefined
  id: number = 0;

  constructor(
    private tareaService: TareaService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = +params['id']; // Convierte el parámetro a número
      this.obtenerTarea(); // Llama a la función para obtener la tarea
    });
  }

  obtenerTarea(): void {
    this.tareaService.obtenerTareaPorId(this.id).subscribe(
      (tarea) => {
        console.log('tarea obtenida:', this.id);
        this.tarea = tarea;
      },
      (error) => {
        console.error('Error al obtener la tarea:', error);
      }
    );
  }
}
