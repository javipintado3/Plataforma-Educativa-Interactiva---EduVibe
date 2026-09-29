import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { ClasesService } from '../../../core/services/clases.service';
import { ConfirmacionService } from '../../../core/services/confirmacion.service';
import { Clase, ModoVistaClase, Pagina } from '../../../core/models';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../shared/estado-vacio/estado-vacio.component';
import { LimpiarFiltrosComponent } from '../../../shared/limpiar-filtros/limpiar-filtros.component';
import { PaginadorComponent } from '../../../shared/paginador/paginador.component';
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
 *
 * Las clases llegan paginadas del servidor, de diez en diez, y la búsqueda por
 * nombre o asignatura también la hace él: filtrar solo lo que hay en la página
 * actual dejaría fuera lo que está en las demás.
 */
@Component({
  selector: 'app-lista-clases',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    TarjetaClaseComponent, EstadoVacioComponent, CargandoComponent, LimpiarFiltrosComponent,
    DialogoComponent, AvisoComponent, SubidaArchivoComponent, PlazoPipe, PaginadorComponent,
  ],
  templateUrl: './lista-clases.component.html',
  styleUrl: './lista-clases.component.css',
})
export class ListaClasesComponent implements OnInit, OnDestroy {

  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  readonly paleta = PALETA_CLASE;

  readonly pagina = signal<Pagina<Clase> | null>(null);
  readonly clases = computed(() => this.pagina()?.contenido ?? []);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly creando = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  // --- gestión (vista de administración) ---
  readonly busqueda = signal('');
  private temporizadorBusqueda?: ReturnType<typeof setTimeout>;

  /**
   * Busca en el servidor, esperando un momento a que se deje de teclear para no
   * lanzar una petición por cada letra. Al cambiar la búsqueda se vuelve a la
   * primera página: la que estábamos viendo podría no existir con menos resultados.
   */
  buscar(texto: string): void {
    this.busqueda.set(texto);
    clearTimeout(this.temporizadorBusqueda);
    this.temporizadorBusqueda = setTimeout(() => this.cargar(0), 300);
  }

  limpiarBusqueda(): void {
    clearTimeout(this.temporizadorBusqueda);
    this.busqueda.set('');
    this.cargar(0);
  }

  // --- selección en lote ---
  readonly seleccionados = signal<Set<string>>(new Set());
  readonly aplicandoLote = signal(false);

  estaSeleccionado(claseId: string): boolean {
    return this.seleccionados().has(claseId);
  }

  alternarSeleccion(clase: Clase, marcado: boolean): void {
    this.seleccionados.update(actuales => {
      const nuevo = new Set(actuales);
      marcado ? nuevo.add(clase.id) : nuevo.delete(clase.id);
      return nuevo;
    });
  }

  get todosSeleccionados(): boolean {
    const visibles = this.clases();
    return visibles.length > 0 && visibles.every(c => this.estaSeleccionado(c.id));
  }

  alternarTodos(marcado: boolean): void {
    const visibles = this.clases();
    this.seleccionados.update(actuales => {
      const nuevo = new Set(actuales);
      for (const clase of visibles) {
        marcado ? nuevo.add(clase.id) : nuevo.delete(clase.id);
      }
      return nuevo;
    });
  }

  limpiarSeleccion(): void {
    this.seleccionados.set(new Set());
  }

  readonly formulario = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    subject: [''],
    color: [PALETA_CLASE[0].valor],
    imageUrl: [''],
    viewMode: this.fb.nonNullable.control<ModoVistaClase>('structured'),
  });

  ngOnInit(): void {
    this.cargar(0);
  }

  ngOnDestroy(): void {
    clearTimeout(this.temporizadorBusqueda);
  }

  cargar(pagina = 0): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.misClases(pagina, this.busqueda()).subscribe({
      next: (resultado) => {
        // Si al borrar la página en la que estábamos se queda vacía, se retrocede a la última que exista
        if (!resultado.contenido.length && resultado.pagina > 0) {
          this.cargar(resultado.totalPaginas - 1);
          return;
        }
        this.pagina.set(resultado);
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
        this.cargar(0);
      },
      error: (err) => {
        this.creando.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  /**
   * Borra las clases seleccionadas. Solo se ofrece a administración, y el
   * backend rechaza el borrado si una clase ya tiene entregas o exámenes
   * hechos: aquí solo se pide confirmación, la regla de negocio vive en el
   * servidor. Si alguna del lote falla, se recarga para reflejar solo lo
   * que sí se llegó a borrar, igual que el lote de usuarios.
   */
  async eliminarSeleccionadas(): Promise<void> {
    const ids = Array.from(this.seleccionados());
    if (!ids.length || this.aplicandoLote()) {
      return;
    }

    const confirmado = await this.confirmacion.preguntar(
      `¿Borrar ${ids.length} clase(s)? Se pierde todo lo que tienen dentro: tareas, exámenes, materiales y matriculaciones.`,
      { titulo: 'Borrar clases', textoConfirmar: 'Borrar' });
    if (!confirmado) {
      return;
    }

    this.aplicandoLote.set(true);
    this.error.set(null);

    forkJoin(ids.map(id => this.clasesService.eliminar(id))).subscribe({
      next: () => {
        this.aplicandoLote.set(false);
        this.limpiarSeleccion();
        this.cargar(this.pagina()?.pagina ?? 0);
      },
      error: (err) => {
        this.aplicandoLote.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
        this.limpiarSeleccion();
        this.cargar(this.pagina()?.pagina ?? 0);
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
