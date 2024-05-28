import { NgModule } from '@angular/core';
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

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'registro', component: RegistroComponent },
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
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
