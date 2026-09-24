import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { TareasService } from '../../../core/services/tareas.service';
import { DetalleTarea, Entrega } from '../../../core/models';
import { AvatarComponent } from '../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../../shared/pastilla-estado/pastilla-estado.component';
import { FechaPipe, PlazoPipe } from '../../../shared/pipes/fecha.pipe';
import { RutaArchivoPipe } from '../../../shared/pipes/ruta-archivo.pipe';
import { SubidaArchivoComponent } from '../../../shared/subida-archivo/subida-archivo.component';

/**
 * Pantalla de una tarea. Es dos pantallas en una, según quién la abra:
 *
 *  - Alumnado: el enunciado y su entrega, con la nota si ya está corregida.
 *  - Profesorado: el enunciado y la lista de entregas para ir corrigiendo.
 *
 * Quién ve qué lo decide la API con el campo puedoEditar, no una comprobación
 * de rol hecha aquí: ser profesor del centro no basta, hay que impartir esta
 * clase, y esa regla vive en el backend.
 */
@Component({
  selector: 'app-detalle-tarea',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent, AvatarComponent,
    DialogoComponent, AvisoComponent, SubidaArchivoComponent, FechaPipe, PlazoPipe, RutaArchivoPipe,
  ],
  templateUrl: './detalle-tarea.component.html',
  styleUrl: './detalle-tarea.component.css',
})
export class DetalleTareaComponent implements OnInit {

  private readonly tareasService = inject(TareasService);
  private readonly fb = inject(FormBuilder);

  @Input() id = '';

  readonly tarea = signal<DetalleTarea | null>(null);
  readonly entregas = signal<Entrega[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  // --- entrega del alumnado ---
  readonly guardando = signal(false);
  readonly mensajeEntrega = signal<string | null>(null);

  readonly formEntrega = this.fb.nonNullable.group({
    content: [''],
    fileUrl: [''],
  });

  // --- corrección ---
  readonly entregaElegida = signal<Entrega | null>(null);
  readonly calificando = signal(false);
  readonly errorNota = signal<string | null>(null);

  readonly formNota = this.fb.nonNullable.group({
    score: [0, [Validators.required, Validators.min(0)]],
    feedback: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);

    this.tareasService.detalle(this.id).subscribe({
      next: (tarea) => {
        this.tarea.set(tarea);
        this.cargando.set(false);

        if (tarea.miEntrega) {
          this.formEntrega.setValue({
            content: tarea.miEntrega.content ?? '',
            fileUrl: tarea.miEntrega.fileUrl ?? '',
          });
        }

        if (tarea.puedoEditar) {
          this.cargarEntregas();
        }
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar la tarea'));
        this.cargando.set(false);
      },
    });
  }

  private cargarEntregas(): void {
    this.tareasService.entregas(this.id).subscribe({
      next: (entregas) => this.entregas.set(entregas),
      error: (err) => this.error.set(AvisoComponent.mensajeDe(err)),
    });
  }

  /** Una entrega ya calificada no se puede tocar: la nota dejaría de corresponder. */
  get bloqueada(): boolean {
    return this.tarea()?.miEntrega?.status === 'graded';
  }

  guardar(enviar: boolean): void {
    if (this.guardando() || this.bloqueada) {
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    this.mensajeEntrega.set(null);

    const { content, fileUrl } = this.formEntrega.getRawValue();

    this.tareasService.guardarMiEntrega(this.id, {
      content: content || undefined,
      fileUrl: fileUrl || undefined,
      enviar,
    }).subscribe({
      next: (entrega) => {
        this.guardando.set(false);
        this.tarea.update(t => t ? { ...t, miEntrega: entrega } : t);
        this.mensajeEntrega.set(enviar ? 'Entrega enviada correctamente.' : 'Borrador guardado.');
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  abrirCorreccion(entrega: Entrega): void {
    this.entregaElegida.set(entrega);
    this.errorNota.set(null);
    this.formNota.setValue({
      score: entrega.grade ? Number(entrega.grade.score) : 0,
      feedback: entrega.grade?.feedback ?? '',
    });
  }

  calificar(): void {
    const entrega = this.entregaElegida();
    if (!entrega || this.calificando() || this.formNota.invalid) {
      return;
    }

    this.calificando.set(true);
    this.errorNota.set(null);

    const { score, feedback } = this.formNota.getRawValue();

    this.tareasService.calificar(entrega.id, score, feedback || undefined).subscribe({
      next: (actualizada) => {
        this.calificando.set(false);
        this.entregaElegida.set(null);
        this.entregas.update(lista =>
          lista.map(e => e.id === actualizada.id ? actualizada : e));
      },
      error: (err) => {
        this.calificando.set(false);
        this.errorNota.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  /** Cuántas entregas quedan por corregir, para la cabecera de la lista. */
  get pendientesDeCorregir(): number {
    return this.entregas().filter(e => e.status === 'submitted').length;
  }
}
