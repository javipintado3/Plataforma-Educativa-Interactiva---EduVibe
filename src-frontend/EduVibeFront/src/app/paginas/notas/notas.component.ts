import { Component, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';

import { ClasesService } from '../../core/services/clases.service';
import { Clase, Entrega } from '../../core/models';
import { mediaPonderada } from '../../core/utils/media-ponderada';
import { AvisoComponent } from '../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../shared/pastilla-estado/pastilla-estado.component';

/** Las notas de una clase, ya con su media, para pintar una tarjeta por materia. */
interface NotasDeClase {
  clase: Clase;
  entregas: Entrega[];
  media: number | null;
  calificadas: number;
}

/**
 * "Notas": el boletín del alumnado, una tarjeta por cada clase en la que está.
 *
 * No hay un endpoint propio: se reutiliza el de la pestaña de calificaciones
 * de cada clase (misEntregas) y se calcula la media con la misma función, en
 * vez de duplicar la lógica de ponderación en el servidor por una pantalla que
 * solo junta lo que ya existe. Como mucho son unas pocas clases por alumno, así
 * que una petición por clase no pesa.
 *
 * No hay una media global entre clases a propósito: mezclar Matemáticas con
 * Historia en un solo número no significa nada, y cada materia se evalúa por
 * separado.
 */
@Component({
  selector: 'app-notas',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink,
    AvisoComponent, CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent,
  ],
  templateUrl: './notas.component.html',
  styleUrl: './notas.component.css',
})
export class NotasComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);

  readonly materias = signal<NotasDeClase[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.clasesService.misClases().pipe(
      switchMap(clases => clases.length
        ? forkJoin(clases.map(clase => this.clasesService.misEntregas(clase.id).pipe(
            map((entregas): NotasDeClase => ({
              clase,
              entregas,
              media: mediaPonderada(entregas),
              calificadas: entregas.filter(entrega => entrega.grade !== null).length,
            })))))
        : of([] as NotasDeClase[])),
    ).subscribe({
      next: (materias) => {
        this.materias.set(materias);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las notas'));
        this.cargando.set(false);
      },
    });
  }
}
