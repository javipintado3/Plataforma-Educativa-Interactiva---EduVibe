import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';

import { Tema } from '../../../../core/models';
import { CrearUnidadDialogoComponent } from './crear-unidad-dialogo.component';
import { PestanaExamenesComponent } from './pestana-examenes.component';
import { PestanaMaterialesComponent } from './pestana-materiales.component';
import { PestanaTrabajoComponent } from './pestana-trabajo.component';

/** Sentinela para el bloque de contenido sin unidad asignada; no puede coincidir con un id real. */
const SIN_UNIDAD = 'sin-unidad';

/**
 * Pestaña "Unidades" (modo `structured`): un selector de tarjetas — una por
 * unidad, más "Sin unidad" y "Nueva unidad" — y, al elegir una, su contenido:
 * Actividades (tareas + exámenes) y Materiales, con un botón para volver al
 * selector.
 *
 * No duplica la lógica de alta ni de listado: reutiliza tal cual
 * app-pestana-trabajo/examenes/materiales, filtradas a la unidad elegida con
 * su input `temaFiltro`.
 */
@Component({
  selector: 'app-pestana-temas',
  standalone: true,
  imports: [
    NgIf, NgFor,
    PestanaTrabajoComponent, PestanaExamenesComponent, PestanaMaterialesComponent, CrearUnidadDialogoComponent,
  ],
  templateUrl: './pestana-temas.component.html',
  styleUrl: './pestana-temas.component.css',
})
export class PestanaTemasComponent {

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;
  @Input() temas: Tema[] = [];

  /** Se emite al crear o renombrar una unidad, para que el padre actualice la clase entera. */
  @Output() temaCreada = new EventEmitter<Tema>();
  @Output() temaActualizada = new EventEmitter<Tema>();

  readonly sinUnidad = SIN_UNIDAD;
  readonly seleccionada = signal<string | null>(null);

  get tituloSeleccionada(): string {
    if (this.seleccionada() === SIN_UNIDAD) {
      return 'Sin unidad';
    }
    return this.temas.find(t => t.id === this.seleccionada())?.title ?? '';
  }

  /** `temaFiltro` que esperan las pestañas embebidas: null para "sin unidad". */
  get temaFiltroActual(): string | null {
    const id = this.seleccionada();
    return id === SIN_UNIDAD ? null : id;
  }

  seleccionar(id: string): void {
    this.seleccionada.set(id);
  }

  volver(): void {
    this.seleccionada.set(null);
  }

  onCreada(tema: Tema): void {
    this.temaCreada.emit(tema);
    this.seleccionada.set(tema.id);
  }

  onActualizada(tema: Tema): void {
    this.temaActualizada.emit(tema);
  }
}
