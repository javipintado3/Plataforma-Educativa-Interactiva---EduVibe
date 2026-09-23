import { Component, Input } from '@angular/core';

/** Indicador de carga centrado, para mientras llega una respuesta. */
@Component({
  selector: 'app-cargando',
  standalone: true,
  template: `
    <div class="caja">
      <span class="cargando"></span>
      <span class="pequeno apagado">{{ texto }}</span>
    </div>
  `,
  styles: [`
    .caja {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 48px 24px;
    }
  `]
})
export class CargandoComponent {
  @Input() texto = 'Cargando…';
}
