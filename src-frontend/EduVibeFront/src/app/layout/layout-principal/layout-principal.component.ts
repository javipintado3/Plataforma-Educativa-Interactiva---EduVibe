import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
import { ConfirmarComponent } from '../../shared/confirmar/confirmar.component';

/**
 * Armazón de las pantallas con sesión iniciada: barra superior, contenido y
 * pie.
 *
 * Es un componente de ruta con hijos, no algo que cada página incluya por su
 * cuenta. Así la barra no se vuelve a montar al navegar entre secciones —se
 * mantiene el menú abierto, el foco, el scroll— y ninguna página puede
 * olvidarse de ponerla.
 *
 * Las pantallas sin sesión (login e invitación) cuelgan fuera de este layout,
 * porque no deben mostrar una barra con opciones que todavía no existen.
 */
@Component({
  selector: 'app-layout-principal',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, ConfirmarComponent],
  template: `
    <div class="armazon">
      <app-navbar></app-navbar>

      <main class="contenido">
        <router-outlet></router-outlet>
      </main>

      <app-footer></app-footer>
    </div>

    <app-confirmar></app-confirmar>
  `,
  styles: [`
    .armazon {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    /* El contenido empuja el pie hasta abajo aunque la página sea corta */
    .contenido { flex: 1; padding: 28px 0 0; }
  `]
})
export class LayoutPrincipalComponent {}
