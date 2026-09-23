import { Component, Input, computed, signal } from '@angular/core';

/**
 * Avatar con las iniciales de la persona.
 *
 * Se prefiere a una foto genérica porque no hay fotos que mostrar y un icono
 * repetido veinte veces en una lista no aporta nada; las iniciales, en cambio,
 * hacen reconocible cada fila de un vistazo.
 */
@Component({
  selector: 'app-avatar',
  standalone: true,
  template: `<span class="iniciales" [class.iniciales-sm]="pequeno" [title]="nombre">{{ iniciales() }}</span>`,
})
export class AvatarComponent {

  private readonly _nombre = signal('');

  @Input({ required: true })
  set nombre(valor: string) {
    this._nombre.set(valor ?? '');
  }
  get nombre(): string {
    return this._nombre();
  }

  @Input() pequeno = false;

  readonly iniciales = computed(() =>
    this._nombre()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(parte => parte[0]!.toUpperCase())
      .join('') || '?'
  );
}
