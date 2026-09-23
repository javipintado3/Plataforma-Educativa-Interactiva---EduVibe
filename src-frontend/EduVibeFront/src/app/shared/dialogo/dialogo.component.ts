import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { NgIf } from '@angular/common';

/**
 * Ventana modal.
 *
 * El contenido llega por proyección, de modo que el mismo componente sirve
 * para crear un usuario, crear una clase, crear una tarea o poner una nota.
 * Las cuatro pantallas solo aportan su formulario.
 */
@Component({
  selector: 'app-dialogo',
  standalone: true,
  imports: [NgIf],
  template: `
    <div class="velo" *ngIf="abierto" (click)="cerrarDesdeElVelo($event)">
      <div class="dialogo" role="dialog" aria-modal="true" [attr.aria-label]="titulo">
        <header class="cabecera">
          <h3>{{ titulo }}</h3>
          <button type="button" class="boton boton-fantasma" (click)="cerrar.emit()" aria-label="Cerrar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
        </header>

        <div class="cuerpo">
          <ng-content></ng-content>
        </div>

        <footer class="pie">
          <ng-content select="[acciones]"></ng-content>
        </footer>
      </div>
    </div>
  `,
  styles: [`
    .velo {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, .45);
      backdrop-filter: blur(2px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      z-index: 1000;
      animation: aparecer .12s ease;
    }

    .dialogo {
      background: #fff;
      border-radius: var(--radio-lg);
      box-shadow: var(--sombra-lg);
      width: 100%;
      max-width: 480px;
      max-height: calc(100vh - 40px);
      display: flex;
      flex-direction: column;
      animation: subir .16s ease;
    }

    .cabecera {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 18px 20px;
      border-bottom: 1px solid var(--borde-sutil);
    }

    .cuerpo { padding: 20px; overflow-y: auto; }

    .pie {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding: 14px 20px;
      border-top: 1px solid var(--borde-sutil);
      background: var(--fondo);
      border-radius: 0 0 var(--radio-lg) var(--radio-lg);
    }

    .pie:empty { display: none; }

    @keyframes aparecer { from { opacity: 0 } }
    @keyframes subir { from { opacity: 0; transform: translateY(8px) } }
  `]
})
export class DialogoComponent {

  @Input() abierto = false;
  @Input({ required: true }) titulo = '';

  /** Si pulsar fuera del recuadro cierra. Se desactiva en formularios largos. */
  @Input() cierraAlPulsarFuera = true;

  @Output() cerrar = new EventEmitter<void>();

  cerrarDesdeElVelo(evento: MouseEvent): void {
    // Solo si el clic es en el velo, no en el recuadro: sin esta comprobación,
    // soltar el ratón dentro tras seleccionar texto cerraría el diálogo
    if (this.cierraAlPulsarFuera && evento.target === evento.currentTarget) {
      this.cerrar.emit();
    }
  }

  @HostListener('document:keydown.escape')
  alPulsarEscape(): void {
    if (this.abierto) {
      this.cerrar.emit();
    }
  }
}
