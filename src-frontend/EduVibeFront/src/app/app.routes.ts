import { Routes } from '@angular/router';

import { LayoutPrincipalComponent } from './layout/layout-principal/layout-principal.component';
import { adminGuard, invitadoGuard, sesionGuard } from './core/guards/sesion.guard';

/**
 * Rutas de la aplicación.
 *
 * Dos zonas bien separadas:
 *
 *  - Fuera del layout: login e invitación. No llevan barra de navegación,
 *    porque quien las ve todavía no tiene sesión y no habría nada que navegar.
 *
 *  - Dentro de LayoutPrincipalComponent: el resto. El guard de sesión se
 *    declara una sola vez, en la ruta padre, y protege a todos los hijos; así
 *    añadir una pantalla nueva no puede dejarla desprotegida por olvido.
 *
 * Todas las páginas se cargan con loadComponent: cada una viaja en su propio
 * fragmento y el navegador solo descarga la que hace falta.
 */
export const routes: Routes = [

  // ------------------------------------------------------------ sin sesión
  {
    path: 'login',
    canActivate: [invitadoGuard],
    title: 'Entrar · Eduvibe',
    loadComponent: () => import('./paginas/login/login.component')
      .then(m => m.LoginComponent),
  },
  {
    path: 'invitacion/:token',
    title: 'Activar cuenta · Eduvibe',
    loadComponent: () => import('./paginas/invitacion/invitacion.component')
      .then(m => m.InvitacionComponent),
  },

  // ------------------------------------------------------------ con sesión
  {
    path: '',
    component: LayoutPrincipalComponent,
    canActivate: [sesionGuard],
    children: [
      { path: '', redirectTo: 'clases', pathMatch: 'full' },

      {
        path: 'clases',
        title: 'Mis clases · Eduvibe',
        loadComponent: () => import('./paginas/clases/lista-clases/lista-clases.component')
          .then(m => m.ListaClasesComponent),
      },
      {
        path: 'clases/:id',
        title: 'Clase · Eduvibe',
        loadComponent: () => import('./paginas/clases/detalle-clase/detalle-clase.component')
          .then(m => m.DetalleClaseComponent),
      },
      {
        path: 'tareas/:id',
        title: 'Tarea · Eduvibe',
        loadComponent: () => import('./paginas/tareas/detalle-tarea/detalle-tarea.component')
          .then(m => m.DetalleTareaComponent),
      },
      {
        path: 'examenes/:id',
        title: 'Examen · Eduvibe',
        loadComponent: () => import('./paginas/examenes/detalle-examen/detalle-examen.component')
          .then(m => m.DetalleExamenComponent),
      },
      {
        path: 'examenes/:id/hacer',
        title: 'Haciendo el examen · Eduvibe',
        loadComponent: () => import('./paginas/examenes/hacer-examen/hacer-examen.component')
          .then(m => m.HacerExamenComponent),
      },
      {
        path: 'calendario',
        title: 'Calendario · Eduvibe',
        loadComponent: () => import('./paginas/calendario/calendario.component')
          .then(m => m.CalendarioComponent),
      },
      {
        path: 'perfil',
        title: 'Mi perfil · Eduvibe',
        loadComponent: () => import('./paginas/perfil/perfil.component')
          .then(m => m.PerfilComponent),
      },
      {
        path: 'admin/usuarios',
        canActivate: [adminGuard],
        title: 'Usuarios · Eduvibe',
        loadComponent: () => import('./paginas/admin/usuarios/usuarios.component')
          .then(m => m.UsuariosComponent),
      },
    ],
  },

  // Cualquier otra cosa vuelve al principio
  { path: '**', redirectTo: '' },
];
