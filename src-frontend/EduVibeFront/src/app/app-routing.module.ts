import { Component, NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegistroComponent } from './auth/registro/registro.component';
import { LoginComponent } from './auth/login/login.component';
import { InicioComponent } from './home/inicio/inicio.component';
import { adminGuard } from './guardians/admin.guard';
import { alumnoGuard } from './guardians/alumno.guard';
import { profesorGuard } from './guardians/profesor.guard';
import { RenewComponent } from './renew/renew.component';
import { loginGuard } from './guardians/login.guard';
import { CalendarioComponent } from './home/calendario/calendario.component';
import { PerfilUsuarioComponent } from './user/perfil-usuario/perfil-usuario.component';
import { MisClasesComponent } from './home/mis-clases/mis-clases.component';
import { VistaDeClaseComponent } from './clase/vista-de-clase/vista-de-clase.component';
import { PersonasDeClaseComponent } from './clase/personas-de-clase/personas-de-clase.component';
import { VistaTareaComponent } from './tarea/vista-tarea/vista-tarea.component';
import { CrearClaseComponent } from './clase/crear-clase/crear-clase.component';
import { adminProfesorGuard } from './guardians/adminProfesor.guard';
import { InscribirUsuarioComponent } from './clase/inscribir-usuario/inscribir-usuario.component';
import { EditarClaseComponent } from './clase/editar-clase/editar-clase.component';
import { CrearTareaComponent } from './tarea/crear-tarea/crear-tarea.component';
import { EditarTareaComponent } from './tarea/editar-tarea/editar-tarea.component';
import { VistaUsersComponent } from './user/vista-users/vista-users.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'registro', component: RegistroComponent,canActivate:[adminGuard] },
  { path: 'login', component: LoginComponent },
  { 
    path: 'inicio', 
    component: InicioComponent, 
    canActivate: [loginGuard] 
  },
  { 
    path: 'calendario', 
    component: CalendarioComponent, 
    canActivate: [loginGuard] 
  },
  { 
    path: "renew", 
    component: RenewComponent, 
    canActivate: [loginGuard]
  },
  {
    path: 'perfil/:idUsuario', 
    component: PerfilUsuarioComponent, 
    canActivate: [loginGuard]
  },
  {
    path: "misClases",
    component:MisClasesComponent,
    canActivate: [loginGuard]
  },
  {
    path: "clase/:id", // Ruta para la vista detallada de una clase, donde ':id' es el identificador de la clase
    component: VistaDeClaseComponent, // Componente que muestra los detalles de la clase
    canActivate: [loginGuard] // Puedes agregar guardias de ruta según sea necesario
  },
  {
    path: "personasClase/:id",
    component:PersonasDeClaseComponent,
    canActivate: [loginGuard] // Puedes agregar guardias de ruta según sea necesario
  },
  {
    path: "tarea/:id",
    component:VistaTareaComponent,
    canActivate: [loginGuard] // Puedes agregar guardias de ruta según sea necesario
  },
  { path: 'crear-clase', component:CrearClaseComponent,
    canActivate: [adminGuard]
   },
   { path: 'inscribir-usuario/:idClase', component: InscribirUsuarioComponent,
    canActivate: [adminGuard]
    },
    { path: 'editar-clase/:idClase', component: EditarClaseComponent,
      canActivate: [adminProfesorGuard]
      },
    {
      path: 'crear-tarea/:idClase', component: CrearTareaComponent,
      canActivate: [adminProfesorGuard]
    },
    {
      path: 'editar-tarea/:idTarea', component: EditarTareaComponent,
      canActivate: [adminProfesorGuard]
    },
    {
      path: 'users', component:VistaUsersComponent,
      canActivate:[adminGuard]
    }


  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
