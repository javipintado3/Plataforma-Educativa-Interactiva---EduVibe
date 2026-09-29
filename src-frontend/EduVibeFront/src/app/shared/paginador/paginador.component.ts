import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIf } from '@angular/common';

/**
 * Controles de paginación: "Anterior", "Página X de Y" y "Siguiente".
 *
 * Es solo la barra: no sabe de dónde salen los datos. Sirve igual para una
 * lista paginada por el servidor (se pide otra página al cambiar) que para una
 * paginada en el propio cliente (se recorta la lista ya cargada), y así todas
 * las pantallas paginan igual y se ven igual.
 *
 * No se pinta con una sola página: una barra que no permite ir a ningún sitio
 * solo estorba.
 */
@Component({
  selector: 'app-paginador',
  standalone: true,
  imports: [NgIf],
  template: `
    <div class="paginacion" *ngIf="totalPaginas > 1">
      <button type="button" class="boton boton-contorno boton-pequeno"
              [disabled]="pagina <= 0" (click)="cambio.emit(pagina - 1)">Anterior</button>
      <span class="pequeno apagado">
        Página {{ pagina + 1 }} de {{ totalPaginas }}<ng-container *ngIf="totalElementos !== null">
          · {{ totalElementos }} {{ etiqueta }}</ng-container>
      </span>
      <button type="button" class="boton boton-contorno boton-pequeno"
              [disabled]="pagina >= totalPaginas - 1" (click)="cambio.emit(pagina + 1)">Siguiente</button>
    </div>
  `,
  styles: [`
    .paginacion {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-wrap: wrap;
      gap: 16px;
      margin-top: 16px;
    }
  `],
})
export class PaginadorComponent {

  /** Página actual, empezando en 0 (como la API). */
  @Input({ required: true }) pagina = 0;

  @Input({ required: true }) totalPaginas = 1;

  /** Total de elementos, si se quiere mostrar junto al número de página. */
  @Input() totalElementos: number | null = null;

  /** Cómo se llama lo que se lista, para acompañar al total: "usuario(s)", "aviso(s)"... */
  @Input() etiqueta = '';

  /** Emite la página (desde 0) a la que se quiere ir. */
  @Output() cambio = new EventEmitter<number>();
}
