import { Component, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

import { TareasService } from '../../core/services/tareas.service';
import { EntregaPorCorregir } from '../../core/models';
import { AvisoComponent } from '../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../shared/estado-vacio/estado-vacio.component';
import { FechaPipe } from '../../shared/pipes/fecha.pipe';

/**
 * "Calificar": la cola de corrección del profesorado, cruzando todas sus clases.
 *
 * Lo que necesita resolver a diario es "qué tengo sin corregir", no navegar
 * hasta la tabla completa de cada clase, que sigue estando dentro de ella.
 * Las más antiguas van primero, porque son las que llevan más tiempo esperando.
 * Cada fila lleva a la tarea, que es donde se corrige.
 */
@Component({
  selector: 'app-correcciones',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, AvisoComponent, CargandoComponent, EstadoVacioComponent, FechaPipe],
  templateUrl: './correcciones.component.html',
})
export class CorreccionesComponent implements OnInit {

  private readonly tareasService = inject(TareasService);

  readonly entregas = signal<EntregaPorCorregir[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.tareasService.porCorregir().subscribe({
      next: (entregas) => {
        this.entregas.set(entregas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las entregas pendientes'));
        this.cargando.set(false);
      },
    });
  }
}
