import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

/**
 * Logotipo de Eduvibe.
 *
 * El anterior era un clipart de libros apilados junto a una tipografía serif
 * que no tenía relación con él. Este parte de la misma idea —la pila de
 * libros— pero reducida a tres barras redondeadas de anchura decreciente, que
 * se leen a la vez como libros apilados y como las barras de un ecualizador:
 * el "vibe" del nombre. El punto suelto remata la barra corta y evita que la
 * composición quede demasiado rígida.
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

        <!-- Pila de tres "libros" de anchura decreciente -->
        <rect x="9" y="24.5" width="22" height="5.2" rx="2.6" fill="#fff" opacity=".95" />
        <rect x="9" y="17.4" width="16" height="5.2" rx="2.6" fill="#fff" opacity=".78" />
        <rect x="9" y="10.3" width="10" height="5.2" rx="2.6" fill="#fff" opacity=".6" />

        <circle cx="23.4" cy="12.9" r="2.6" fill="#fff" opacity=".95" />
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
