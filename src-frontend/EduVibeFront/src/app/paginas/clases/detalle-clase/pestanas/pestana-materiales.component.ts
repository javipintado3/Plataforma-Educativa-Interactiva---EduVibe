import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ClasesService } from '../../../../core/services/clases.service';
import { ConfirmacionService } from '../../../../core/services/confirmacion.service';
import { MaterialesService } from '../../../../core/services/materiales.service';
import { Material, Tema, TipoMaterial } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../../../shared/pastilla-estado/pastilla-estado.component';

/**
 * Pestaña "Materiales": apuntes, enlaces y vídeos de la clase.
 *
 * El mismo diálogo sirve para crear y para editar —como en la mayoría de
 * pantallas de la aplicación—, distinguido por `editando`: null es alta,
 * un material es edición.
 */
@Component({
  selector: 'app-pestana-materiales',
  standalone: true,
  imports: [
    NgIf, NgFor, ReactiveFormsModule,
    CargandoComponent, EstadoVacioComponent, DialogoComponent, AvisoComponent, PastillaEstadoComponent,
  ],
  templateUrl: './pestana-materiales.component.html',
  styleUrl: './pestana-materiales.component.css',
})
export class PestanaMaterialesComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly materialesService = inject(MaterialesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;
  @Input() temas: Tema[] = [];

  readonly materiales = signal<Material[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly borrando = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly guardando = signal(false);
  readonly errorFormulario = signal<string | null>(null);
  readonly editando = signal<Material | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    fileUrl: [''],
    type: this.fb.nonNullable.control<TipoMaterial | ''>(''),
    topicId: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.materiales(this.claseId).subscribe({
      next: (materiales) => {
        this.materiales.set(materiales);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar los materiales'));
        this.cargando.set(false);
      },
    });
  }

  tituloDelTema(topicId: string | null): string {
    if (!topicId) {
      return 'Sin tema';
    }
    return this.temas.find(t => t.id === topicId)?.title ?? 'Sin tema';
  }

  abrirCrear(): void {
    this.editando.set(null);
    this.formulario.reset({ title: '', fileUrl: '', type: '', topicId: '' });
    this.errorFormulario.set(null);
    this.dialogoAbierto.set(true);
  }

  abrirEditar(material: Material): void {
    this.editando.set(material);
    this.formulario.reset({
      title: material.title,
      fileUrl: material.fileUrl ?? '',
      type: material.type ?? '',
      topicId: material.topicId ?? '',
    });
    this.errorFormulario.set(null);
    this.dialogoAbierto.set(true);
  }

  guardar(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.guardando()) {
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);

    const { title, fileUrl, type, topicId } = this.formulario.getRawValue();
    // El @Pattern del backend admite null pero no "": sin esto, dejar el tipo
    // sin especificar devolvía 400 en vez de guardar el material.
    const datos = { title, fileUrl: fileUrl || undefined, type: type || undefined, topicId: topicId || null };
    const edicion = this.editando();

    const peticion = edicion
      ? this.materialesService.actualizar(edicion.id, datos)
      : this.clasesService.crearMaterial(this.claseId, datos);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.dialogoAbierto.set(false);
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async borrar(material: Material): Promise<void> {
    const confirmado = await this.confirmacion.preguntar(`¿Retirar "${material.title}" de los materiales?`, {
      titulo: 'Retirar material', textoConfirmar: 'Retirar',
    });
    if (!confirmado) {
      return;
    }

    this.borrando.set(material.id);
    this.error.set(null);

    this.materialesService.eliminar(material.id).subscribe({
      next: () => {
        this.borrando.set(null);
        this.materiales.update(lista => lista.filter(m => m.id !== material.id));
      },
      error: (err) => {
        this.borrando.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
