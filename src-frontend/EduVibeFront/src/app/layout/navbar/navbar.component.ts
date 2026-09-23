import { Component, HostListener, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { LogoComponent } from '../../shared/logo/logo.component';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { PastillaEstadoComponent } from '../../shared/pastilla-estado/pastilla-estado.component';

/**
 * Barra superior.
 *
 * Los enlaces que se muestran dependen del rol, y no solo por comodidad:
 * enseñar "Usuarios" a un alumno para que después reciba un 403 es una forma
 * de mentirle sobre lo que puede hacer.
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [NgIf, RouterLink, RouterLinkActive, LogoComponent, AvatarComponent, PastillaEstadoComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {

  readonly auth = inject(AuthService);

  readonly menuAbierto = signal(false);
  readonly navegacionAbierta = signal(false);

  alternarMenu(): void {
    this.menuAbierto.update(abierto => !abierto);
  }

  alternarNavegacion(): void {
    this.navegacionAbierta.update(abierta => !abierta);
  }

  cerrarTodo(): void {
    this.menuAbierto.set(false);
    this.navegacionAbierta.set(false);
  }

  salir(): void {
    this.cerrarTodo();
    this.auth.logout();
  }

  /** Cierra el menú al pulsar en cualquier otro sitio de la página. */
  @HostListener('document:click', ['$event'])
  alPulsarFuera(evento: MouseEvent): void {
    const objetivo = evento.target as HTMLElement;
    if (!objetivo.closest('[data-menu-usuario]')) {
      this.menuAbierto.set(false);
    }
  }
}
