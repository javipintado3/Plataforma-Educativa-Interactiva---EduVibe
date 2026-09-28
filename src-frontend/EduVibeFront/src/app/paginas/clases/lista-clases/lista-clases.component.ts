import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from '../../../core/services/auth.service';
import { ClasesService } from '../../../core/services/clases.service';
import { ConfirmacionService } from '../../../core/services/confirmacion.service';
import { Clase, ModoVistaClase } from '../../../core/models';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../shared/estado-vacio/estado-vacio.component';
import { LimpiarFiltrosComponent } from '../../../shared/limpiar-filtros/limpiar-filtros.component';
import { PALETA_CLASE } from '../../../shared/paleta-clase';
import { PlazoPipe } from '../../../shared/pipes/fecha.pipe';
import { SubidaArchivoComponent } from '../../../shared/subida-archivo/subida-archivo.component';
import { TarjetaClaseComponent } from '../../../shared/tarjeta-clase/tarjeta-clase.component';

/**
 * Panel principal: las clases de quien entra.
 *
 * Cada rol ve lo suyo sin que esta pantalla filtre nada: el backend ya
 * devuelve las clases que corresponden. Duplicar aquí esa decisión sería
 * mantener la misma regla en dos sitios.
 */
@Component({
  selector: 'app-lista-clases',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    TarjetaClaseComponent, EstadoVacioComponent, CargandoComponent, LimpiarFiltrosComponent,
    DialogoComponent, AvisoComponent, SubidaArchivoComponent, PlazoPipe,
  ],
  templateUrl: './lista-clases.component.html',
  styleUrl: './lista-clases.component.css',
})
export class ListaClasesComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  readonly paleta = PALETA_CLASE;

  readonly clases = signal<Clase[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly creando = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  // --- gestión (vista de administración) ---
  readonly busqueda = signal('');
  readonly borrando = signal<string | null>(null);

  readonly clasesFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) {
      return this.clases();
    }
    return this.clases().filter(c =>
      c.name.toLowerCase().includes(texto) || (c.subject ?? '').toLowerCase().includes(texto));
  });

  readonly formulario = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    subject: [''],
    color: [PALETA_CLASE[0].valor],
    imageUrl: [''],
    viewMode: this.fb.nonNullable.control<ModoVistaClase>('structured'),
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.misClases().subscribe({
      next: (clases) => {
        this.clases.set(clases);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las clases'));
        this.cargando.set(false);
      },
    });
  }

  abrirDialogo(): void {
    this.formulario.reset({ name: '', subject: '', color: PALETA_CLASE[0].valor, imageUrl: '', viewMode: 'structured' });
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

    const { name, subject, color, imageUrl, viewMode } = this.formulario.getRawValue();

    this.clasesService.crear({
      name,
      subject: subject || undefined,
      color,
      imageUrl: imageUrl.trim() || undefined,
      viewMode,
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

  /**
   * Borra la clase entera. Solo se ofrece a administración, y el backend
   * rechaza el borrado si ya hay entregas o exámenes hechos: aquí solo se
   * pide confirmación, la regla de negocio vive en el servidor.
   */
  async eliminar(clase: Clase, evento: Event): Promise<void> {
    evento.preventDefault();
    evento.stopPropagation();

    const confirmado = await this.confirmacion.preguntar(
      `¿Borrar "${clase.name}"? Se pierde todo lo que tiene dentro: tareas, exámenes, materiales y matriculaciones.`,
      { titulo: 'Borrar clase', textoConfirmar: 'Borrar' });
    if (!confirmado) {
      return;
    }

    this.borrando.set(clase.id);
    this.error.set(null);

    this.clasesService.eliminar(clase.id).subscribe({
      next: () => {
        this.borrando.set(null);
        this.clases.update(lista => lista.filter(c => c.id !== clase.id));
      },
      error: (err) => {
        this.borrando.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  /** Texto del estado vacío: no es lo mismo no tener clases que no tener ninguna creada. */
  get tituloVacio(): string {
    return this.auth.esAdmin() ? 'Todavía no hay clases' : 'No estás en ninguna clase';
  }

  get descripcionVacia(): string {
    return this.auth.esAdmin()
      ? 'Crea la primera clase del centro y matricula al profesorado y al alumnado.'
      : 'Cuando te matriculen en una clase, aparecerá aquí.';
  }
}
