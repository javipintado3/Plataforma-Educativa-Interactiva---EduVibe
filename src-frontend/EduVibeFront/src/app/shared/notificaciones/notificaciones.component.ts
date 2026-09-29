import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';

import { NotificacionesService } from '../../core/services/notificaciones.service';
import { Notificacion, Pagina } from '../../core/models';
import { CargandoComponent } from '../cargando/cargando.component';
import { EstadoVacioComponent } from '../estado-vacio/estado-vacio.component';
import { FechaPipe } from '../pipes/fecha.pipe';
import { PaginadorComponent } from '../paginador/paginador.component';
import { rutaDeNotificacion } from './ruta-notificacion';

/**
 * Campana de notificaciones del navbar, visible en toda la app.
 *
 * El contador de no leídas se pide al arrancar (una llamada ligera); la
 * lista completa solo se trae la primera vez que se abre el desplegable,
 * para no cargar veinte notificaciones en cada cambio de página si nadie
 * llega a abrirlo.
 */
@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [NgIf, NgFor, CargandoComponent, EstadoVacioComponent, FechaPipe, PaginadorComponent],
  templateUrl: './notificaciones.component.html',
  styleUrl: './notificaciones.component.css',
})
export class NotificacionesComponent implements OnInit {

  private readonly notificacionesService = inject(NotificacionesService);
  private readonly router = inject(Router);

  readonly abierto = signal(false);
  readonly notificaciones = signal<Notificacion[]>([]);
  /** Datos de paginación de lo que se está viendo; la lista la pagina el servidor. */
  readonly pagina = signal<Pagina<Notificacion> | null>(null);
  readonly cargando = signal(false);
  readonly cargadas = signal(false);
  readonly noLeidas = signal(0);
  readonly marcandoTodas = signal(false);

  ngOnInit(): void {
    this.notificacionesService.noLeidas().subscribe(contador => this.noLeidas.set(contador));
  }

  alternar(): void {
    this.abierto.update(estaba => !estaba);
    if (this.abierto() && !this.cargadas()) {
      this.cargar();
    }
  }

  cargar(pagina = 0): void {
    this.cargando.set(true);
    this.notificacionesService.misNotificaciones(pagina).subscribe(resultado => {
      this.notificaciones.set(resultado.contenido);
      this.pagina.set(resultado);
      this.cargando.set(false);
      this.cargadas.set(true);
    });
  }

  /** Marca leída y, si la notificación trae dónde ir, navega. */
  irA(notificacion: Notificacion): void {
    this.marcarLeida(notificacion);
    this.abierto.set(false);

    const ruta = rutaDeNotificacion(notificacion);
    if (ruta) {
      this.router.navigateByUrl(ruta);
    }
  }

  private marcarLeida(notificacion: Notificacion): void {
    if (notificacion.leida) {
      return;
    }
    this.notificacionesService.marcarLeida(notificacion.id).subscribe(() => {
      this.notificaciones.update(lista =>
        lista.map(n => n.id === notificacion.id ? { ...n, leida: true } : n));
      this.noLeidas.update(contador => Math.max(0, contador - 1));
    });
  }

  marcarTodasLeidas(evento: Event): void {
    evento.stopPropagation();
    if (this.marcandoTodas() || !this.noLeidas()) {
      return;
    }
    this.marcandoTodas.set(true);

    this.notificacionesService.marcarTodasLeidas().subscribe({
      next: () => {
        this.marcandoTodas.set(false);
        this.notificaciones.update(lista => lista.map(n => ({ ...n, leida: true })));
        this.noLeidas.set(0);
      },
      error: () => this.marcandoTodas.set(false),
    });
  }

  /** Cierra el desplegable al pulsar en cualquier otro sitio de la página. */
  @HostListener('document:click', ['$event'])
  alPulsarFuera(evento: MouseEvent): void {
    const objetivo = evento.target as HTMLElement;
    // Un elemento que ya se ha quitado del DOM (p. ej. al cambiar de página) no dice nada sobre dónde se pulsó
    if (!objetivo.isConnected) {
      return;
    }
    if (!objetivo.closest('[data-menu-notificaciones]')) {
      this.abierto.set(false);
    }
  }
}
