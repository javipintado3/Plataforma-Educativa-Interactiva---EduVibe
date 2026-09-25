import { Component, Input, computed, signal } from '@angular/core';
import { NgIf } from '@angular/common';

import { RutaArchivoPipe } from '../pipes/ruta-archivo.pipe';

/**
 * Avatar de la persona: su foto si tiene una subida, si no sus iniciales.
 *
 * Sin foto se prefieren las iniciales a un icono genérico, porque un icono
 * repetido veinte veces en una lista no aporta nada; las iniciales, en
 * cambio, hacen reconocible cada fila de un vistazo.
 */
@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [NgIf, RutaArchivoPipe],
  template: `
    <img *ngIf="avatarUrl" class="foto-avatar" [class.foto-avatar-sm]="pequeno" [class.foto-avatar-lg]="grande"
         [src]="avatarUrl | rutaArchivo" [alt]="nombre">
    <span *ngIf="!avatarUrl" class="iniciales" [class.iniciales-sm]="pequeno" [class.iniciales-lg]="grande"
          [title]="nombre">{{ iniciales() }}</span>
  `,
  styles: [`
    .foto-avatar {
      width: 38px;
      height: 38px;
      border-radius: 999px;
      object-fit: cover;
      display: block;
      flex-shrink: 0;
    }
    .foto-avatar-sm { width: 30px; height: 30px; }
    .foto-avatar-lg { width: 84px; height: 84px; }
    .iniciales-lg { width: 84px; height: 84px; font-size: 1.6rem; }
  `],
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
  @Input() grande = false;
  @Input() avatarUrl: string | null = null;

  readonly iniciales = computed(() =>
    this._nombre()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(parte => parte[0]!.toUpperCase())
      .join('') || '?'
  );
}
