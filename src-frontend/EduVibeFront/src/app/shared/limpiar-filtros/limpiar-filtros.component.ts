import { Component, EventEmitter, Output } from '@angular/core';

/**
 * Botón "Limpiar filtros", con el mismo icono en todas las pantallas que
 * tienen un formulario de filtros (usuarios, clases...). Antes cada pantalla
 * tenía su propio botón de texto suelto; esto evita que un icono nuevo tenga
 * que copiarse a mano en cada sitio donde haga falta.
 */
@Component({
  selector: 'app-limpiar-filtros',
  standalone: true,
  template: `
    <button type="button" class="boton boton-fantasma" (click)="limpiar.emit()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M13 3H2l8 9.46V19l4 2v-8.54l.9-1.06"/>
        <path d="m22 3-5 5"/>
        <path d="m17 3 5 5"/>
      </svg>
      Limpiar
    </button>
  `,
})
export class LimpiarFiltrosComponent {
  @Output() limpiar = new EventEmitter<void>();
}
