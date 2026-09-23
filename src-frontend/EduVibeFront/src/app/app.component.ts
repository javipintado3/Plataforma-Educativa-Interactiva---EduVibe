import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Raíz de la aplicación.
 *
 * No pinta nada por su cuenta: la barra y el pie viven en
 * LayoutPrincipalComponent, que es una ruta, para que las pantallas sin sesión
 * puedan quedarse fuera de ese armazón.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>',
})
export class AppComponent {}
