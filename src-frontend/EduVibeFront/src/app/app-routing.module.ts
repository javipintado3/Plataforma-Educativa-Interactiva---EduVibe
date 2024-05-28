import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegistroComponent } from './auth/registro/registro.component';
import { LoginComponent } from './auth/login/login.component';
import { InicioComponent } from './home/inicio/inicio.component';
import { adminGuard } from './guardians/admin.guard';
import { alumnoGuard } from './guardians/alumno.guard';
import { profesorGuard } from './guardians/profesor.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'registro', component: RegistroComponent },
  { path: 'login', component: LoginComponent },
  { 
    path: 'inicio', 
    component: InicioComponent, 
    
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }


