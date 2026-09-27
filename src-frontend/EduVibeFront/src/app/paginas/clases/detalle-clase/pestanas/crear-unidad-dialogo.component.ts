import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ClasesService } from '../../../../core/services/clases.service';
import { Tema } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';

/**
 * Diálogo de alta y renombrado de una unidad (o módulo, en modo flexible).
 *
 * El mismo diálogo sirve para las dos cosas —como ya se hace en "Nuevo
 * material"/"Editar material"—, distinguido por `editando`: null es alta, una
 * unidad es renombrado. Se abre desde fuera con `abrir()`/`editar()`
 * (referencia de plantilla), como ya se hace con otros diálogos que necesitan
 * dispararse desde un padre sin duplicar su estado.
 */
@Component({
  selector: 'app-crear-unidad-dialogo',
  standalone: true,
  imports: [NgIf, ReactiveFormsModule, DialogoComponent, AvisoComponent],
  templateUrl: './crear-unidad-dialogo.component.html',
})
export class CrearUnidadDialogoComponent {

  private readonly clasesService = inject(ClasesService);
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) claseId!: string;
  @Input() etiquetaUnidad = 'Unidad';

  /** Género de {@link etiquetaUnidad}, para el artículo: "Nueva unidad" vs "Nuevo módulo". */
  @Input() genero: 'f' | 'm' = 'f';

  @Output() creada = new EventEmitter<Tema>();
  @Output() actualizada = new EventEmitter<Tema>();

  readonly abierto = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly editando = signal<Tema | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
  });

  get tituloDialogo(): string {
    if (this.editando()) {
      return 'Editar ' + this.etiquetaUnidad.toLowerCase();
    }
    return (this.genero === 'm' ? 'Nuevo ' : 'Nueva ') + this.etiquetaUnidad.toLowerCase();
  }

  get tituloBoton(): string {
    return this.editando() ? 'Guardar cambios' : 'Crear ' + this.etiquetaUnidad.toLowerCase();
  }

  get estaEste(): string {
    return this.genero === 'm' ? 'este' : 'esta';
  }

  abrir(): void {
    this.editando.set(null);
    this.formulario.reset({ title: '' });
    this.error.set(null);
    this.abierto.set(true);
  }

  editar(tema: Tema): void {
    this.editando.set(tema);
    this.formulario.reset({ title: tema.title });
    this.error.set(null);
    this.abierto.set(true);
  }

  cerrar(): void {
    this.abierto.set(false);
  }

  guardar(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.guardando()) {
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    const title = this.formulario.getRawValue().title;
    const edicion = this.editando();

    const peticion = edicion
      ? this.clasesService.actualizarTema(this.claseId, edicion.id, title)
      : this.clasesService.crearTema(this.claseId, title);

    peticion.subscribe({
      next: (tema) => {
        this.guardando.set(false);
        this.abierto.set(false);
        edicion ? this.actualizada.emit(tema) : this.creada.emit(tema);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
