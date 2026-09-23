import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ClasesService } from '../../../../core/services/clases.service';
import { Entrega } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../../../shared/pastilla-estado/pastilla-estado.component';
import { FechaPipe } from '../../../../shared/pipes/fecha.pipe';

/**
 * Pestaña "Calificaciones" del alumnado: sus entregas en esta clase, con la
 * nota de las que ya están corregidas y la media de esas.
 *
 * La media solo cuenta lo calificado. Incluir en ella lo que aún no se ha
 * corregido daría un número que baja solo porque el profesor va con retraso.
 */
@Component({
  selector: 'app-pestana-calificaciones',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink,
    CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent, AvisoComponent, FechaPipe,
  ],
  templateUrl: './pestana-calificaciones.component.html',
  styleUrl: './pestana-calificaciones.component.css',
})
export class PestanaCalificacionesComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);

  @Input({ required: true }) claseId!: string;

  readonly entregas = signal<Entrega[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly calificadas = computed(() => this.entregas().filter(e => e.grade !== null));

  readonly media = computed(() => {
    const notas = this.calificadas();
    if (!notas.length) {
      return null;
    }
    const suma = notas.reduce((total, entrega) => total + Number(entrega.grade!.score), 0);
    return Math.round((suma / notas.length) * 10) / 10;
  });

  ngOnInit(): void {
    this.cargando.set(true);

    this.clasesService.misEntregas(this.claseId).subscribe({
      next: (entregas) => {
        this.entregas.set(entregas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las calificaciones'));
        this.cargando.set(false);
      },
    });
  }
}
