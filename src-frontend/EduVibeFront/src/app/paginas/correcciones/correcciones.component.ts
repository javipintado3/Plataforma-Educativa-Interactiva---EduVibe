import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

import { TareasService } from '../../core/services/tareas.service';
import { EntregaPorCorregir, Pagina } from '../../core/models';
import { AvisoComponent } from '../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../shared/estado-vacio/estado-vacio.component';
import { FechaPipe } from '../../shared/pipes/fecha.pipe';
import { PaginadorComponent } from '../../shared/paginador/paginador.component';

/**
 * "Calificar": la cola de corrección del profesorado, cruzando todas sus clases.
 *
 * Lo que necesita resolver a diario es "qué tengo sin corregir", no navegar
 * hasta la tabla completa de cada clase, que sigue estando dentro de ella.
 * Las más antiguas van primero, porque son las que llevan más tiempo esperando.
 * Cada fila lleva a la tarea, que es donde se corrige.
 *
 * La cola se pagina en el servidor, de diez en diez: un profesor con muchas
 * clases puede tener cientos de entregas pendientes y no hace falta traerlas todas.
 */
@Component({
  selector: 'app-correcciones',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, AvisoComponent, CargandoComponent, EstadoVacioComponent, FechaPipe, PaginadorComponent],
  templateUrl: './correcciones.component.html',
})
export class CorreccionesComponent implements OnInit {

  private readonly tareasService = inject(TareasService);

  readonly pagina = signal<Pagina<EntregaPorCorregir> | null>(null);
  readonly entregas = computed(() => this.pagina()?.contenido ?? []);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar(0);
  }

  cargar(pagina: number): void {
    this.cargando.set(true);
    this.error.set(null);

    this.tareasService.porCorregir(pagina).subscribe({
      next: (resultado) => {
        this.pagina.set(resultado);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las entregas pendientes'));
        this.cargando.set(false);
      },
    });
  }
}
