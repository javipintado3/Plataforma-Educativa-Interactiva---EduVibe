import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

type Tipo = 'error' | 'exito' | 'info' | 'atencion';

/**
 * Mensaje de error o de confirmación.
 *
 * Además de pintarlo, sabe sacar el texto de un HttpErrorResponse. Es la parte
 * que más se repetía: cada pantalla interpretaba el error a su manera, y en la
 * versión anterior todas acababan mostrando "usuario y/o contraseña
 * incorrectos" incluso cuando el servidor estaba caído.
 */
@Component({
  selector: 'app-aviso',
  standalone: true,
  imports: [NgIf],
  template: `
    <div class="aviso" [class]="'aviso aviso-' + tipo" *ngIf="mensaje" role="alert">
      <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor"
           stroke-width="2" stroke-linecap="round" style="flex-shrink:0;margin-top:1px">
        <path *ngIf="tipo === 'exito'" d="M20 6 9 17l-5-5"/>
        <ng-container *ngIf="tipo !== 'exito'">
          <circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16.5v.01"/>
        </ng-container>
      </svg>
      <span>{{ mensaje }}</span>
    </div>
  `,
  styles: [`
    /* Ocupa su línea y respeta los márgenes que le pongan desde fuera.
       Sin esto, cada plantilla tenía que añadirle una clase para lo mismo. */
    :host { display: block; }
    :host:empty { display: none; }
  `],
})
export class AvisoComponent {

  @Input() tipo: Tipo = 'error';
  @Input() mensaje: string | null = null;

  /**
   * Convierte un fallo HTTP en algo que una persona pueda leer.
   *
   * Es estático para poder usarlo desde cualquier componente sin inyectar
   * nada: `this.error = AvisoComponent.mensajeDe(err)`.
   */
  static mensajeDe(error: unknown, porDefecto = 'No se ha podido completar la operación'): string {
    if (!(error instanceof HttpErrorResponse)) {
      return porDefecto;
    }

    // El servidor no responde: no tiene sentido hablar de credenciales
    if (error.status === 0) {
      return 'No se ha podido contactar con el servidor. Comprueba que la API está levantada.';
    }

    // Errores de validación campo a campo
    const campos = error.error?.campos as Record<string, string> | undefined;
    if (campos && Object.keys(campos).length) {
      return Object.values(campos).join('. ');
    }

    return error.error?.message || porDefecto;
  }
}
