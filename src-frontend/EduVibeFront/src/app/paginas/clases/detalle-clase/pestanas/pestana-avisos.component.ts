import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ClasesService } from '../../../../core/services/clases.service';
import { ConfirmacionService } from '../../../../core/services/confirmacion.service';
import { Anuncio, Pagina } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { FechaPipe } from '../../../../shared/pipes/fecha.pipe';
import { PaginadorComponent } from '../../../../shared/paginador/paginador.component';

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
  imports: [NgIf, NgFor, ReactiveFormsModule, CargandoComponent, EstadoVacioComponent, DialogoComponent, AvisoComponent, FechaPipe,
    PaginadorComponent],
  templateUrl: './pestana-avisos.component.html',
  styleUrl: './pestana-avisos.component.css',
})
export class PestanaAvisosComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;

  /** La página de avisos que se está viendo: la pagina el servidor, de diez en diez. */
  readonly pagina = signal<Pagina<Anuncio> | null>(null);
  readonly avisos = computed(() => this.pagina()?.contenido ?? []);
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

  cargar(pagina = 0): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.avisos(this.claseId, pagina).subscribe({
      next: (resultado) => {
        // Si al retirar un aviso la página en la que estábamos se queda sin nada, se retrocede a la última
        if (!resultado.contenido.length && resultado.pagina > 0) {
          this.cargar(resultado.totalPaginas - 1);
          return;
        }
        this.pagina.set(resultado);
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
        // El aviso nuevo va arriba del muro, así que se vuelve a la primera página
        this.cargar(0);
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
        // Se vuelve a pedir la página: el servidor sube el aviso que estaba en la siguiente
        this.cargar(this.pagina()?.pagina ?? 0);
      },
      error: (err) => {
        this.borrando.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
