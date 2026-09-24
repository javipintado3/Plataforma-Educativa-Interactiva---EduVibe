import { Component, inject } from '@angular/core';
import { NgIf } from '@angular/common';

import { ConfirmacionService } from '../../core/services/confirmacion.service';
import { DialogoComponent } from '../dialogo/dialogo.component';

/**
 * Diálogo de confirmación único para toda la aplicación.
 *
 * Se monta una sola vez en `LayoutPrincipalComponent`, fuera del
 * `router-outlet`: así cualquier pantalla puede pedir una confirmación
 * inyectando {@link ConfirmacionService} sin tener que colocar su propio
 * `app-dialogo` para eso.
 */
@Component({
  selector: 'app-confirmar',
  standalone: true,
  imports: [NgIf, DialogoComponent],
  template: `
    <app-dialogo *ngIf="peticion() as p" [abierto]="true" [titulo]="p.titulo"
                 [cierraAlPulsarFuera]="false" (cerrar)="responder(false)">
      <p class="mb-0">{{ p.mensaje }}</p>

      <ng-container acciones>
        <button type="button" class="boton boton-contorno" (click)="responder(false)">Cancelar</button>
        <button type="button" [class]="p.peligro ? 'boton boton-peligro' : 'boton boton-primario'"
                (click)="responder(true)">{{ p.textoConfirmar }}</button>
      </ng-container>
    </app-dialogo>
  `,
})
export class ConfirmarComponent {

  private readonly confirmacion = inject(ConfirmacionService);
  readonly peticion = this.confirmacion.peticion;

  responder(confirmado: boolean): void {
    this.confirmacion.responder(confirmado);
  }
}
