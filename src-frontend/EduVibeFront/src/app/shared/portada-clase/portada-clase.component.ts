import { Component, Input, computed, signal } from '@angular/core';
import { NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';

/**
 * Portada de una clase.
 *
 * Si la clase tiene imagen propia, se muestra. Si no, se compone una a partir
 * de su identificador: seis motivos geométricos que van rotando, teñidos con
 * el color de la asignatura.
 *
 * Se generan en lugar de usar fotos de archivo por tres motivos:
 *
 *  - Siempre cargan. Una demo no puede depender de que un servicio de imágenes
 *    externo siga en pie, ni tardar en pintar mientras descarga fotos.
 *  - Pesan unos cientos de bytes en vez de cientos de kilobytes.
 *  - Combinan con el color de la clase, así que el conjunto sigue leyéndose
 *    como un sistema y no como un collage.
 *
 * La elección es determinista: la misma clase enseña siempre el mismo motivo,
 * de modo que sirve para reconocerla, pero dos clases seguidas casi nunca
 * coinciden.
 */
@Component({
  selector: 'app-portada-clase',
  standalone: true,
  imports: [NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault],
  template: `
    <div class="portada" [style.--tono]="color || '#059669'" [style.height.px]="alto">

      <!-- Portada propia de la clase -->
      <img *ngIf="imageUrl" class="foto" [src]="imageUrl" [alt]="'Portada de ' + titulo" loading="lazy">

      <!-- Portada compuesta -->
      <svg *ngIf="!imageUrl" class="motivo" viewBox="0 0 320 120" preserveAspectRatio="xMidYMid slice"
           aria-hidden="true">
        <rect width="320" height="120" [attr.fill]="'url(#f' + idUnico + ')'"/>
        <defs>
          <linearGradient [attr.id]="'f' + idUnico" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stop-color="var(--tono)" stop-opacity=".92"/>
            <stop offset="100%" stop-color="var(--tono)" stop-opacity="1"/>
          </linearGradient>
        </defs>

        <g fill="none" stroke="#fff" stroke-opacity=".28" stroke-width="2" [ngSwitch]="variante()">

          <!-- 0 · círculos concéntricos -->
          <g *ngSwitchCase="0">
            <circle cx="262" cy="26" r="16"/><circle cx="262" cy="26" r="30"/>
            <circle cx="262" cy="26" r="46"/><circle cx="262" cy="26" r="62"/>
            <circle cx="46" cy="104" r="20" fill="#fff" fill-opacity=".14" stroke="none"/>
          </g>

          <!-- 1 · ondas -->
          <g *ngSwitchCase="1">
            <path d="M-10 78 Q 45 46, 100 78 T 210 78 T 320 78"/>
            <path d="M-10 96 Q 45 64, 100 96 T 210 96 T 320 96"/>
            <path d="M-10 114 Q 45 82, 100 114 T 210 114 T 320 114"/>
            <circle cx="268" cy="32" r="14" fill="#fff" fill-opacity=".16" stroke="none"/>
          </g>

          <!-- 2 · rejilla de puntos -->
          <g *ngSwitchCase="2" fill="#fff" fill-opacity=".3" stroke="none">
            <circle cx="216" cy="24" r="3"/><circle cx="244" cy="24" r="3"/><circle cx="272" cy="24" r="3"/><circle cx="300" cy="24" r="3"/>
            <circle cx="216" cy="52" r="3"/><circle cx="244" cy="52" r="3"/><circle cx="272" cy="52" r="3"/><circle cx="300" cy="52" r="3"/>
            <circle cx="216" cy="80" r="3"/><circle cx="244" cy="80" r="3"/><circle cx="272" cy="80" r="3"/><circle cx="300" cy="80" r="3"/>
            <rect x="24" y="60" width="72" height="72" rx="16" fill-opacity=".12"/>
          </g>

          <!-- 3 · triángulos -->
          <g *ngSwitchCase="3">
            <path d="M232 96 L268 34 L304 96 Z"/>
            <path d="M196 96 L226 44 L256 96 Z" stroke-opacity=".18"/>
            <path d="M-6 120 L40 56 L86 120 Z" fill="#fff" fill-opacity=".1" stroke="none"/>
          </g>

          <!-- 4 · diagonales -->
          <g *ngSwitchCase="4" stroke-opacity=".22">
            <path d="M180 130 L250 -10"/><path d="M208 130 L278 -10"/>
            <path d="M236 130 L306 -10"/><path d="M264 130 L334 -10"/>
            <circle cx="52" cy="34" r="22" fill="#fff" fill-opacity=".12" stroke="none"/>
          </g>

          <!-- 5 · formas orgánicas -->
          <g *ngSwitchDefault>
            <path d="M262 4 C298 4, 322 32, 314 62 C306 92, 272 104, 248 88 C224 72, 226 4, 262 4 Z"
                  fill="#fff" fill-opacity=".13" stroke="none"/>
            <path d="M26 118 C8 96, 22 62, 50 62 C78 62, 92 92, 76 114"/>
          </g>
        </g>
      </svg>
    </div>
  `,
  styles: [`
    .portada {
      position: relative;
      width: 100%;
      overflow: hidden;
      background: var(--tono);
      display: block;
    }

    .foto, .motivo {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: cover;
    }
  `]
})
export class PortadaClaseComponent {

  /** Identificador de la clase: decide qué motivo le toca. */
  private readonly _semilla = signal('');

  @Input({ required: true })
  set semilla(valor: string) {
    this._semilla.set(valor ?? '');
  }

  @Input() imageUrl: string | null = null;
  @Input() color: string | null = null;
  @Input() titulo = '';
  @Input() alto = 96;

  /**
   * Motivo que le corresponde a esta clase.
   *
   * Se suman los códigos de los caracteres del identificador y se toma el
   * resto entre seis. Es estable —la clase enseña siempre el mismo— y reparte
   * bien, porque los identificadores son UUID y sus caracteres son variados.
   */
  readonly variante = computed(() => {
    const semilla = this._semilla();
    let suma = 0;
    for (let i = 0; i < semilla.length; i++) {
      suma += semilla.charCodeAt(i);
    }
    return suma % 6;
  });

  /** Cada portada necesita su propio degradado; ids repetidos se pisan. */
  readonly idUnico = Math.random().toString(36).slice(2, 9);
}
