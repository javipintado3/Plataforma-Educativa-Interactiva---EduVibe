import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),

    provideRouter(
      routes,
      // Los parámetros de la ruta llegan a los componentes como @Input, sin
      // tener que inyectar ActivatedRoute y suscribirse en cada pantalla
      withComponentInputBinding(),
      // Al navegar se vuelve arriba; al usar atrás, se recupera la posición
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled' }),
    ),

    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
