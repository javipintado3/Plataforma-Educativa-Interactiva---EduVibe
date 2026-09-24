import { Injectable, signal } from '@angular/core';

interface PeticionConfirmacion {
  titulo: string;
  mensaje: string;
  textoConfirmar: string;
  peligro: boolean;
  resolver: (confirmado: boolean) => void;
}

/**
 * Confirmación de una acción, sin `window.confirm()`.
 *
 * El diálogo nativo no se puede estilar, bloquea el hilo del navegador entero
 * y no es accesible. Aquí el estado vive en una señal que lee un único
 * `ConfirmarComponent` montado una vez en el layout, y `preguntar()` devuelve
 * una promesa para que el resto del código se lea igual que antes:
 * `if (!(await confirmacion.preguntar(...))) return;`
 */
@Injectable({ providedIn: 'root' })
export class ConfirmacionService {

  private readonly _peticion = signal<PeticionConfirmacion | null>(null);
  readonly peticion = this._peticion.asReadonly();

  preguntar(mensaje: string, opciones?: { titulo?: string; textoConfirmar?: string; peligro?: boolean }): Promise<boolean> {
    return new Promise<boolean>((resolver) => {
      this._peticion.set({
        titulo: opciones?.titulo ?? 'Confirmar',
        mensaje,
        textoConfirmar: opciones?.textoConfirmar ?? 'Confirmar',
        peligro: opciones?.peligro ?? true,
        resolver,
      });
    });
  }

  responder(confirmado: boolean): void {
    this._peticion()?.resolver(confirmado);
    this._peticion.set(null);
  }
}
