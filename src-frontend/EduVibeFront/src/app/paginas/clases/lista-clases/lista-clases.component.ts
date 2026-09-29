import { Component, ElementRef, NgZone, OnDestroy, OnInit, computed, effect, inject, signal, viewChild } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

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

/** Debe coincidir con minmax(268px, 1fr) y gap de .rejilla en el CSS: es la misma cuenta que hace el grid. */
const ANCHO_MIN_TARJETA = 268;
const GAP_REJILLA = 18;

/** Filas por página en la vista de tarjetas: la página ocupa una pantalla sin scroll, sea cual sea su ancho. */
const FILAS_POR_PAGINA = 2;

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
export class ListaClasesComponent implements OnInit, OnDestroy {

  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);
  private readonly zone = inject(NgZone);
  readonly auth = inject(AuthService);

  readonly paleta = PALETA_CLASE;

  readonly clases = signal<Clase[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly dialogoAbierto = signal(false);
  readonly creando = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  // --- tarjetas (profesorado y alumnado): paginación según lo que quepa en pantalla ---
  private readonly rejillaEl = viewChild<ElementRef<HTMLDivElement>>('rejilla');
  private observador?: ResizeObserver;

  readonly anchoRejilla = signal(0);
  readonly paginaActual = signal(0);

  /** Las mismas columnas que calcula el grid con auto-fill: no se duplica el número a mano en ningún sitio. */
  readonly columnas = computed(() =>
    Math.max(1, Math.floor((this.anchoRejilla() + GAP_REJILLA) / (ANCHO_MIN_TARJETA + GAP_REJILLA))));

  readonly tamPagina = computed(() => this.columnas() * FILAS_POR_PAGINA);

  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.clases().length / this.tamPagina())));

  /** Si la ventana crece y sobran páginas, la actual se recorta sin tocar la señal: no hace falta escribirla desde un effect. */
  readonly paginaEfectiva = computed(() => Math.min(this.paginaActual(), this.totalPaginas() - 1));

  readonly clasesPagina = computed(() => {
    const tam = this.tamPagina();
    const inicio = this.paginaEfectiva() * tam;
    return this.clases().slice(inicio, inicio + tam);
  });

  constructor() {
    // El contenedor de la rejilla aparece y desaparece (solo existe para
    // profesorado/alumnado, y solo con clases cargadas), así que el
    // ResizeObserver se conecta y desconecta cada vez que cambia, en vez de
    // engancharse una sola vez en ngAfterViewInit.
    effect(() => {
      const elemento = this.rejillaEl()?.nativeElement;
      this.observador?.disconnect();

      if (!elemento) {
        return;
      }
      this.observador = new ResizeObserver(entradas => {
        const ancho = entradas[0].contentRect.width;
        this.zone.run(() => this.anchoRejilla.set(ancho));
      });
      this.observador.observe(elemento);
    });
  }

  ngOnDestroy(): void {
    this.observador?.disconnect();
  }

  paginaAnterior(): void {
    this.paginaActual.set(Math.max(0, this.paginaEfectiva() - 1));
  }

  paginaSiguiente(): void {
    this.paginaActual.set(Math.min(this.totalPaginas() - 1, this.paginaEfectiva() + 1));
  }

  // --- gestión (vista de administración) ---
  readonly busqueda = signal('');

  readonly clasesFiltradas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    if (!texto) {
      return this.clases();
    }
    return this.clases().filter(c =>
      c.name.toLowerCase().includes(texto) || (c.subject ?? '').toLowerCase().includes(texto));
  });

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
    const visibles = this.clasesFiltradas();
    return visibles.length > 0 && visibles.every(c => this.estaSeleccionado(c.id));
  }

  alternarTodos(marcado: boolean): void {
    const visibles = this.clasesFiltradas();
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
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.misClases().subscribe({
      next: (clases) => {
        this.clases.set(clases);
        this.paginaActual.set(0);
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
        this.cargar();
      },
      error: (err) => {
        this.aplicandoLote.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
        this.limpiarSeleccion();
        this.cargar();
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
