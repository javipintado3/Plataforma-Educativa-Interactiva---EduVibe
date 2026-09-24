import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ClasesService } from '../../../../core/services/clases.service';
import { ConfirmacionService } from '../../../../core/services/confirmacion.service';
import { Anuncio } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { FechaPipe } from '../../../../shared/pipes/fecha.pipe';

/**
 * Pestaña "Avisos": el muro de la clase.
 *
 * Solo el profesorado de la clase (o administración) publica y retira avisos;
 * `puedoEditar` llega del padre, que ya lo sabe por el detalle de la clase.
 * Quién puede borrar CADA aviso concreto lo decide la API en `puedoBorrar`.
 */
@Component({
  selector: 'app-pestana-avisos',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule, CargandoComponent, EstadoVacioComponent, DialogoComponent, AvisoComponent, FechaPipe],
  templateUrl: './pestana-avisos.component.html',
  styleUrl: './pestana-avisos.component.css',
})
export class PestanaAvisosComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;

  readonly avisos = signal<Anuncio[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly borrando = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly publicando = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    content: ['', [Validators.required, Validators.maxLength(5000)]],
    pinned: [false],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.avisos(this.claseId).subscribe({
      next: (avisos) => {
        this.avisos.set(avisos);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar los avisos'));
        this.cargando.set(false);
      },
    });
  }

  abrirDialogo(): void {
    this.formulario.reset({ content: '', pinned: false });
    this.errorFormulario.set(null);
    this.dialogoAbierto.set(true);
  }

  publicar(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.publicando()) {
      return;
    }

    this.publicando.set(true);
    this.errorFormulario.set(null);

    const { content, pinned } = this.formulario.getRawValue();

    this.clasesService.crearAviso(this.claseId, { content, pinned }).subscribe({
      next: () => {
        this.publicando.set(false);
        this.dialogoAbierto.set(false);
        this.cargar();
      },
      error: (err) => {
        this.publicando.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async borrar(aviso: Anuncio): Promise<void> {
    const confirmado = await this.confirmacion.preguntar('¿Retirar este aviso del muro?', {
      titulo: 'Retirar aviso', textoConfirmar: 'Retirar',
    });
    if (!confirmado) {
      return;
    }

    this.borrando.set(aviso.id);
    this.error.set(null);

    this.clasesService.borrarAviso(aviso.id).subscribe({
      next: () => {
        this.borrando.set(null);
        this.avisos.update(lista => lista.filter(a => a.id !== aviso.id));
      },
      error: (err) => {
        this.borrando.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
