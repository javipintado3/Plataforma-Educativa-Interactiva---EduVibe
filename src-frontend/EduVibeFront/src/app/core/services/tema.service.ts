import { effect, Injectable, signal } from '@angular/core';

type Tema = 'claro' | 'oscuro';

const CLAVE_ALMACENAMIENTO = 'eduvibe-tema';

/**
 * Modo claro/oscuro de toda la app.
 *
 * El tema vive en una señal y se aplica como `data-tema` en `<html>`, para que
 * sea `styles.css` quien decida los colores (ya centralizados en variables) y
 * no cada componente. `index.html` aplica el mismo atributo antes de que
 * Angular arranque, para no ver un parpadeo del tema claro al recargar en
 * oscuro.
 */
@Injectable({ providedIn: 'root' })
export class TemaService {

  private readonly _tema = signal<Tema>(leerPreferenciaInicial());
  readonly tema = this._tema.asReadonly();

  constructor() {
    effect(() => {
      document.documentElement.setAttribute('data-tema', this._tema());
      localStorage.setItem(CLAVE_ALMACENAMIENTO, this._tema());
    });
  }

  alternar(): void {
    this._tema.update(actual => actual === 'oscuro' ? 'claro' : 'oscuro');
  }
}

function leerPreferenciaInicial(): Tema {
  const guardado = localStorage.getItem(CLAVE_ALMACENAMIENTO);
  if (guardado === 'claro' || guardado === 'oscuro') {
    return guardado;
  }
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'oscuro' : 'claro';
}
