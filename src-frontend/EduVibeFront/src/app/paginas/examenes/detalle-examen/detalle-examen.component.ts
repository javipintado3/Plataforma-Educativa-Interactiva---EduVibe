import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { ConfirmacionService } from '../../../core/services/confirmacion.service';
import { ExamenesService } from '../../../core/services/examenes.service';
import { DetalleExamen, IntentoResumenExamen, PreguntaExamen } from '../../../core/models';
import { AvatarComponent } from '../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../../shared/pastilla-estado/pastilla-estado.component';
import { FechaPipe, PlazoPipe } from '../../../shared/pipes/fecha.pipe';

/**
 * Pantalla de aterrizaje de un examen. Es dos pantallas en una, según quién la abra:
 *
 *  - Alumnado: el estado de su propio intento, y el botón para empezarlo,
 *    retomarlo, o la nota si ya lo entregó. Las preguntas no se ven aquí: solo
 *    al empezar el intento, para que el tiempo cuente desde ese momento.
 *  - Profesorado: cuántas preguntas y puntos tiene, la lista de intentos del
 *    alumnado, y un repaso de las preguntas con la respuesta correcta marcada.
 */
@Component({
  selector: 'app-detalle-examen',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink,
    CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent, AvatarComponent,
    AvisoComponent, FechaPipe, PlazoPipe,
  ],
  templateUrl: './detalle-examen.component.html',
  styleUrl: './detalle-examen.component.css',
})
export class DetalleExamenComponent implements OnInit {

  private readonly examenesService = inject(ExamenesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly router = inject(Router);

  @Input() id = '';

  readonly examen = signal<DetalleExamen | null>(null);
  readonly intentos = signal<IntentoResumenExamen[]>([]);
  readonly preguntas = signal<PreguntaExamen[]>([]);
  readonly mostrarPreguntas = signal(false);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly empezando = signal(false);
  readonly borrando = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);

    this.examenesService.detalle(this.id).subscribe({
      next: (examen) => {
        this.examen.set(examen);
        this.cargando.set(false);

        if (examen.puedoEditar) {
          this.cargarIntentos();
        }
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar el examen'));
        this.cargando.set(false);
      },
    });
  }

  private cargarIntentos(): void {
    this.examenesService.intentos(this.id).subscribe({
      next: (intentos) => this.intentos.set(intentos),
      error: (err) => this.error.set(AvisoComponent.mensajeDe(err)),
    });
  }

  verPreguntas(): void {
    if (this.mostrarPreguntas()) {
      this.mostrarPreguntas.set(false);
      return;
    }

    this.examenesService.preguntas(this.id).subscribe({
      next: (preguntas) => {
        this.preguntas.set(preguntas);
        this.mostrarPreguntas.set(true);
      },
      error: (err) => this.error.set(AvisoComponent.mensajeDe(err)),
    });
  }

  /** Empieza el examen, o retoma el intento en curso: el backend decide cuál de las dos. */
  empezar(): void {
    if (this.empezando()) {
      return;
    }
    this.empezando.set(true);
    this.error.set(null);

    this.examenesService.comenzarOReanudar(this.id).subscribe({
      next: () => this.router.navigate(['/examenes', this.id, 'hacer']),
      error: (err) => {
        this.empezando.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async eliminar(): Promise<void> {
    const examen = this.examen();
    if (!examen || this.borrando()) {
      return;
    }

    const confirmado = await this.confirmacion.preguntar(`¿Borrar el examen "${examen.title}"?`, {
      titulo: 'Borrar examen', textoConfirmar: 'Borrar',
    });
    if (!confirmado) {
      return;
    }

    this.borrando.set(true);
    this.error.set(null);

    this.examenesService.eliminar(this.id).subscribe({
      next: () => this.router.navigate(['/clases', examen.classId]),
      error: (err) => {
        this.borrando.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
