import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

/**
 * Logotipo de Eduvibe.
 *
 * Un birrete de graduación muy simplificado sobre un cuadrado verde con
 * degradado. La borla amarilla es el único toque de color extra y aporta la
 * energía del "vibe" del nombre.
 *
 * Al ser SVG se ve nítido a cualquier tamaño, cambia de color por CSS y pesa
 * unos cientos de bytes en lugar de los kilobytes de un PNG.
 */
@Component({
  selector: 'app-logo',
  standalone: true,
  imports: [NgIf],
  template: `
    <span class="logo" [style.--tamano.px]="size">
      <svg class="logo-marca" viewBox="0 0 40 40" role="img" [attr.aria-label]="showWordmark ? null : 'Eduvibe'">
        <defs>
          <linearGradient [attr.id]="idDegradado" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" [attr.stop-color]="mono ? 'currentColor' : '#34d399'" />
            <stop offset="100%" [attr.stop-color]="mono ? 'currentColor' : '#047857'" />
          </linearGradient>
        </defs>

        <rect width="40" height="40" rx="11" [attr.fill]="'url(#' + idDegradado + ')'" />

        <!-- Birrete de graduación (dibujado sobre una cuadrícula de 64, escalado a 40) -->
        <g transform="scale(.625)">
          <path d="M32 14 L57 26 L32 38 L7 26 Z" fill="#fff" stroke="#fff" stroke-width="3" stroke-linejoin="round" />
          <path d="M18 34 V42 Q32 50 46 42 V34 L32 41 Z" fill="#fff" opacity=".85" />
          <path d="M55 27 V42" stroke="#fff" stroke-width="3" stroke-linecap="round" fill="none" />
          <circle cx="55" cy="46" r="4" [attr.fill]="mono ? '#fff' : '#fbbf24'" />
        </g>
      </svg>

      <span class="logo-texto" *ngIf="showWordmark">
        <span class="logo-edu">Edu</span><span class="logo-vibe">Vibe</span>
      </span>
    </span>
  `,
  styles: [`
    .logo {
      display: inline-flex;
      align-items: center;
      gap: calc(var(--tamano) * .28);
      line-height: 1;
    }

    .logo-marca {
      width: var(--tamano);
      height: var(--tamano);
      display: block;
      flex-shrink: 0;
    }

    .logo-texto {
      font-weight: 700;
      font-size: calc(var(--tamano) * .62);
      letter-spacing: -.025em;
      white-space: nowrap;
    }

    .logo-edu  { color: var(--tinta); }
    .logo-vibe { color: var(--verde-600); }

    /* Sobre fondo oscuro o de color, todo el logotipo en blanco */
    :host(.sobre-color) .logo-edu,
    :host(.sobre-color) .logo-vibe { color: #fff; }
  `]
})
export class LogoComponent {

  /** Lado del cuadrado de la marca, en píxeles. El texto escala con él. */
  @Input() size = 32;

  /** Si se acompaña del nombre escrito. */
  @Input() showWordmark = true;

  /** Usa el color heredado en lugar del verde, para fondos de color. */
  @Input() mono = false;

  /**
   * Cada instancia necesita su propio degradado: dos <defs> con el mismo id en
   * la misma página se pisan y el segundo logotipo se pintaría con el color
   * del primero.
   */
  readonly idDegradado = 'logo-degradado-' + Math.random().toString(36).slice(2, 9);
}
