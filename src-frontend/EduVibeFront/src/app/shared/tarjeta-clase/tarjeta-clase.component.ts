import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Clase } from '../../core/models';
import { PlazoPipe } from '../pipes/fecha.pipe';

/**
 * Tarjeta de una clase para el panel principal.
 *
 * La franja de color superior es la identidad de la asignatura en toda la
 * aplicación: permite reconocer la clase sin leer el título.
 *
 * La línea de estado —la próxima entrega— es lo que hace útil esta pantalla.
 * Sin ella habría que entrar en cada clase para saber qué toca.
 */
@Component({
  selector: 'app-tarjeta-clase',
  standalone: true,
  imports: [NgIf, RouterLink, PlazoPipe],
  template: `
    <a class="tarjeta tarjeta-clase" [routerLink]="['/clases', clase.id]">
      <span class="franja" [style.background]="clase.color || 'var(--verde-500)'"></span>

      <div class="cuerpo">
        <div class="fila-entre" style="align-items:flex-start">
          <div>
            <p class="asignatura">{{ clase.subject || 'Sin asignatura' }}</p>
            <h3 class="nombre">{{ clase.name }}</h3>
          </div>
          <span class="pastilla pastilla-verde" *ngIf="clase.miRol === 'teacher'">Impartes</span>
          <span class="pastilla pastilla-azul" *ngIf="clase.miRol === 'admin'">Centro</span>
        </div>

        <p class="profes" *ngIf="clase.profesores.length">
          {{ clase.profesores.join(', ') }}
        </p>
        <p class="profes" *ngIf="!clase.profesores.length">Sin profesorado asignado</p>
      </div>

      <div class="pie" [class.urgente]="esUrgente()">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>
        </svg>
        <span>{{ clase.proximaEntrega ? (clase.proximaEntrega | plazo) : 'Sin tareas pendientes' }}</span>
      </div>
    </a>
  `,
  styles: [`
    .tarjeta-clase {
      display: block;
      overflow: hidden;
      text-decoration: none;
      color: inherit;
      transition: box-shadow .16s ease, transform .16s ease, border-color .16s ease;
    }

    .tarjeta-clase:hover {
      text-decoration: none;
      box-shadow: var(--sombra-md);
      transform: translateY(-2px);
      border-color: var(--verde-200);
    }

    .franja { display: block; height: 5px; }

    .cuerpo { padding: 16px 18px 14px; }

    .asignatura {
      font-size: .72rem;
      font-weight: 650;
      text-transform: uppercase;
      letter-spacing: .06em;
      color: var(--gris);
      margin: 0 0 2px;
    }

    .nombre { font-size: 1.02rem; margin: 0; }

    .profes {
      font-size: .84rem;
      color: var(--gris);
      margin: 10px 0 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .pie {
      display: flex;
      align-items: center;
      gap: 7px;
      padding: 10px 18px;
      border-top: 1px solid var(--borde-sutil);
      background: var(--fondo);
      font-size: .82rem;
      color: var(--gris);
    }

    .pie.urgente { color: var(--ambar); background: var(--ambar-50); }
  `]
})
export class TarjetaClaseComponent {

  @Input({ required: true }) clase!: Clase;

  /** Se resalta cuando quedan menos de tres días. */
  esUrgente(): boolean {
    if (!this.clase.proximaEntrega) {
      return false;
    }
    const dias = (new Date(this.clase.proximaEntrega).getTime() - Date.now()) / 86_400_000;
    return dias <= 3;
  }
}
