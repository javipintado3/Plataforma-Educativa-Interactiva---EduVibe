import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ClasesService } from '../../../../core/services/clases.service';
import { HiloForo, Tema } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { FechaPipe } from '../../../../shared/pipes/fecha.pipe';

/**
 * Pestaña "Foro": los hilos de debate de la clase, con más actividad
 * reciente primero.
 *
 * Cualquier persona matriculada puede abrir un hilo, no solo el
 * profesorado: el foro es un espacio de la clase entera. Al crearlo se
 * navega directamente a su detalle, porque un hilo recién abierto sin
 * mensaje no tendría sentido y el primer mensaje ya se manda en el mismo gesto.
 */
@Component({
  selector: 'app-pestana-foro',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    CargandoComponent, EstadoVacioComponent, DialogoComponent, AvisoComponent, FechaPipe,
  ],
  templateUrl: './pestana-foro.component.html',
  styleUrl: './pestana-foro.component.css',
})
export class PestanaForoComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) claseId!: string;
  @Input() temas: Tema[] = [];

  /**
   * Filtra a un solo tema: `undefined` = todos (comportamiento por defecto),
   * un id de tema = solo ese, `null` = solo lo que no tiene tema.
   */
  @Input() temaFiltro: string | null | undefined = undefined;

  /** Cómo llamar al agrupador en este modo de vista: "Unidad" o "Módulo". */
  @Input() etiquetaUnidad = 'Unidad';

  readonly hilos = signal<HiloForo[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly abriendo = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    content: ['', [Validators.required]],
    topicId: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.hilosDeForo(this.claseId).subscribe({
      next: (hilos) => {
        this.hilos.set(hilos);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar los hilos'));
        this.cargando.set(false);
      },
    });
  }

  /** Los hilos que toca pintar, según {@link temaFiltro}. */
  get hilosVisibles(): HiloForo[] {
    const todos = this.hilos();
    if (this.temaFiltro === undefined) {
      return todos;
    }
    return todos.filter(h => h.topicId === this.temaFiltro);
  }

  get etiquetaUnidadMinuscula(): string {
    return this.etiquetaUnidad.toLowerCase();
  }

  get etiquetaSinUnidad(): string {
    return 'Sin ' + this.etiquetaUnidadMinuscula;
  }

  tituloDelTema(topicId: string | null): string {
    if (!topicId) {
      return this.etiquetaSinUnidad;
    }
    return this.temas.find(t => t.id === topicId)?.title ?? this.etiquetaSinUnidad;
  }

  abrirDialogo(): void {
    this.formulario.reset({ title: '', content: '', topicId: this.temaFiltro ?? '' });
    this.errorFormulario.set(null);
    this.dialogoAbierto.set(true);
  }

  crear(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.abriendo()) {
      return;
    }

    this.abriendo.set(true);
    this.errorFormulario.set(null);

    const { title, content, topicId } = this.formulario.getRawValue();

    this.clasesService.abrirHilo(this.claseId, { title, content, topicId: topicId || null }).subscribe({
      next: (hilo) => {
        this.abriendo.set(false);
        this.dialogoAbierto.set(false);
        this.router.navigate(['/foro', hilo.id]);
      },
      error: (err) => {
        this.abriendo.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
