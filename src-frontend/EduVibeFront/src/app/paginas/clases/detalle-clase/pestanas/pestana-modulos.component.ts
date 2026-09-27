import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';

import { Tema } from '../../../../core/models';
import { CrearUnidadDialogoComponent } from './crear-unidad-dialogo.component';
import { PestanaExamenesComponent } from './pestana-examenes.component';
import { PestanaForoComponent } from './pestana-foro.component';
import { PestanaMaterialesComponent } from './pestana-materiales.component';
import { PestanaTrabajoComponent } from './pestana-trabajo.component';

/** Sentinela para el bloque de contenido sin módulo asignado; no puede coincidir con un id real. */
const SIN_MODULO = 'sin-modulo';

/**
 * Pestaña "Módulos" (modo `flexible`): el mismo selector de tarjetas que
 * "Unidades", pero al entrar en uno se apilan las cuatro secciones (trabajo,
 * exámenes, materiales, foro) bajo un único encabezado — sin separar
 * actividades de materiales, como pide el modo universidad.
 *
 * Simplificación consciente: son las mismas cuatro secciones apiladas y
 * filtradas al módulo, no una lista literalmente entreverada por fecha.
 */
@Component({
  selector: 'app-pestana-modulos',
  standalone: true,
  imports: [
    NgIf, NgFor,
    PestanaTrabajoComponent, PestanaExamenesComponent, PestanaMaterialesComponent, PestanaForoComponent,
    CrearUnidadDialogoComponent,
  ],
  templateUrl: './pestana-modulos.component.html',
  styleUrl: './pestana-temas.component.css',
})
export class PestanaModulosComponent {

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;
  @Input() temas: Tema[] = [];

  /** Se emite al crear o renombrar un módulo, para que el padre actualice la clase entera. */
  @Output() temaCreada = new EventEmitter<Tema>();
  @Output() temaActualizada = new EventEmitter<Tema>();

  readonly sinModulo = SIN_MODULO;
  readonly seleccionado = signal<string | null>(null);

  get tituloSeleccionado(): string {
    if (this.seleccionado() === SIN_MODULO) {
      return 'Sin módulo';
    }
    return this.temas.find(t => t.id === this.seleccionado())?.title ?? '';
  }

  /** `temaFiltro` que esperan las pestañas embebidas: null para "sin módulo". */
  get temaFiltroActual(): string | null {
    const id = this.seleccionado();
    return id === SIN_MODULO ? null : id;
  }

  seleccionar(id: string): void {
    this.seleccionado.set(id);
  }

  volver(): void {
    this.seleccionado.set(null);
  }

  onCreada(tema: Tema): void {
    this.temaCreada.emit(tema);
    this.seleccionado.set(tema.id);
  }

  onActualizada(tema: Tema): void {
    this.temaActualizada.emit(tema);
  }
}
