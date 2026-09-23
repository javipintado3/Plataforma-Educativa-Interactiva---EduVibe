import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

/**
 * Lo que se muestra cuando una lista no tiene nada.
 *
 * Una tabla vacía sin explicación parece un fallo; decir qué falta y qué se
 * puede hacer convierte el hueco en una indicación.
 */
@Component({
  selector: 'app-estado-vacio',
  standalone: true,
  imports: [NgIf],
  template: `
    <div class="vacio">
      <div class="vacio-icono">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 5.5A1.5 1.5 0 0 1 5.5 4H10a2 2 0 0 1 2 2 2 2 0 0 1 2-2h4.5A1.5 1.5 0 0 1 20 5.5v12a1.5 1.5 0 0 1-1.5 1.5H14a2 2 0 0 0-2 2 2 2 0 0 0-2-2H5.5A1.5 1.5 0 0 1 4 17.5z"/>
        </svg>
      </div>
      <p class="fuerte mb-1" style="color: var(--tinta-70)">{{ titulo }}</p>
      <p class="pequeno mb-0" *ngIf="descripcion">{{ descripcion }}</p>
      <ng-content></ng-content>
    </div>
  `,
})
export class EstadoVacioComponent {
  @Input({ required: true }) titulo = '';
  @Input() descripcion = '';
}
