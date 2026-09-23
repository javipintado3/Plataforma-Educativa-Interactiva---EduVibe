import { Component } from '@angular/core';

import { LogoComponent } from '../../shared/logo/logo.component';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [LogoComponent],
  template: `
    <footer class="pie">
      <div class="contenedor fila-entre envolver espaciado">
        <app-logo [size]="22"></app-logo>
        <p class="pequeno apagado mb-0">
          Plataforma educativa · {{ anio }}
        </p>
      </div>
    </footer>
  `,
  styles: [`
    .pie {
      border-top: 1px solid var(--borde);
      background: var(--blanco);
      padding: 20px 0;
      margin-top: 56px;
    }
  `]
})
export class FooterComponent {
  readonly anio = new Date().getFullYear();
}
