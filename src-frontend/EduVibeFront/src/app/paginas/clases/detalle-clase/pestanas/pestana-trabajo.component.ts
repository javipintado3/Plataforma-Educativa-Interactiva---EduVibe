import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgClass, NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ClasesService } from '../../../../core/services/clases.service';
import { Tarea, Tema } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../../../shared/pastilla-estado/pastilla-estado.component';
import { FechaPipe, PlazoPipe } from '../../../../shared/pipes/fecha.pipe';

/** Una tarea ya emparejada con el tema al que pertenece. */
interface Bloque {
  tema: Tema | null;
  tareas: Tarea[];
}

/**
 * Pestaña "Trabajo de clase": las tareas, agrupadas por tema.
 *
 * Cada fila muestra lo que le interesa a quien mira: al alumnado, el estado de
 * su entrega; al profesorado, cuántas lleva recibidas. Esa distinción la hace
 * la API, y aquí solo se pinta el campo que venga relleno.
 */
@Component({
  selector: 'app-pestana-trabajo',
  standalone: true,
  imports: [
    NgIf, NgFor, NgClass, RouterLink, ReactiveFormsModule,
    CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent,
    DialogoComponent, AvisoComponent, FechaPipe, PlazoPipe,
  ],
  templateUrl: './pestana-trabajo.component.html',
  styleUrl: './pestana-trabajo.component.css',
})
export class PestanaTrabajoComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;
  @Input() temas: Tema[] = [];

  readonly bloques = signal<Bloque[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly creando = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    title: ['', [Validators.required]],
    description: [''],
    dueDate: [''],
    points: [100, [Validators.required, Validators.min(1)]],
    topicId: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.tareas(this.claseId).subscribe({
      next: (tareas) => {
        this.bloques.set(this.agrupar(tareas));
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las tareas'));
        this.cargando.set(false);
      },
    });
  }

  /**
   * Reparte las tareas por tema conservando el orden que ya trae la API (por
   * fecha de entrega), y deja al final las que no cuelgan de ningún tema.
   */
  private agrupar(tareas: Tarea[]): Bloque[] {
    const porTema = new Map<string, Tarea[]>();
    const sueltas: Tarea[] = [];

    for (const tarea of tareas) {
      if (!tarea.topicId) {
        sueltas.push(tarea);
        continue;
      }
      const lista = porTema.get(tarea.topicId) ?? [];
      lista.push(tarea);
      porTema.set(tarea.topicId, lista);
    }

    const bloques: Bloque[] = this.temas
      .filter(tema => porTema.has(tema.id))
      .map(tema => ({ tema, tareas: porTema.get(tema.id)! }));

    if (sueltas.length) {
      bloques.push({ tema: null, tareas: sueltas });
    }
    return bloques;
  }

  /** Estado que se muestra al alumnado: sin entrega todavía cuenta como "sin empezar". */
  estadoDe(tarea: Tarea): 'draft' | 'submitted' | 'graded' | 'sin-empezar' {
    return tarea.miEstado ?? 'sin-empezar';
  }

  abrirDialogo(): void {
    this.formulario.reset({ title: '', description: '', dueDate: '', points: 100, topicId: '' });
    this.errorFormulario.set(null);
    this.dialogoAbierto.set(true);
  }

  crear(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.creando()) {
      return;
    }

    this.creando.set(true);
    this.errorFormulario.set(null);

    const { title, description, dueDate, points, topicId } = this.formulario.getRawValue();

    this.clasesService.crearTarea(this.claseId, {
      title,
      description: description || undefined,
      // El input datetime-local da "2026-10-02T18:30"; la API espera un
      // instante en UTC, y eso es justo lo que hace toISOString()
      dueDate: dueDate ? new Date(dueDate).toISOString() : null,
      points,
      topicId: topicId || null,
    }).subscribe({
      next: () => {
        this.creando.set(false);
        this.dialogoAbierto.set(false);
        this.cargar();
      },
      error: (err) => {
        this.creando.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
