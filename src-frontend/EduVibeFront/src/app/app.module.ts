import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, RouterModule, provideRouter, withComponentInputBinding } from '@angular/router';
import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptorInterceptor } from './interceptors/jwt-interceptor.interceptor';
import { AppComponent } from './app.component';
import { AppRoutingModule, routes } from './app-routing.module';
import { NavbarComponent } from './layout/navbar/navbar.component';
import { FooterComponent } from './layout/footer/footer.component';
import { RegistroComponent } from './auth/registro/registro.component';
import { LoginComponent } from './auth/login/login.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { InicioComponent } from './home/inicio/inicio.component';
import { NgxPaginationModule } from 'ngx-pagination';
import { CalendarioComponent } from './home/calendario/calendario.component';
import { FullCalendarModule } from '@fullcalendar/angular';
import { PerfilUsuarioComponent } from './user/perfil-usuario/perfil-usuario.component';
import { MisClasesComponent } from './home/mis-clases/mis-clases.component';
import { VistaDeClaseComponent } from './clase/vista-de-clase/vista-de-clase.component';
import { NavbarSecundarioComponent } from './layout/navbar-secundario/navbar-secundario.component';
import { PersonasDeClaseComponent } from './clase/personas-de-clase/personas-de-clase.component';
import { VistaTareaComponent } from './tarea/vista-tarea/vista-tarea.component';
import { CrearClaseComponent } from './clase/crear-clase/crear-clase.component';
import { InscribirUsuarioComponent } from './clase/inscribir-usuario/inscribir-usuario.component';
import { EditarClaseComponent } from './clase/editar-clase/editar-clase.component';
import { CrearTareaComponent } from './tarea/crear-tarea/crear-tarea.component';
import { EditarTareaComponent } from './tarea/editar-tarea/editar-tarea.component';
import { VistaUsersComponent } from './user/vista-users/vista-users.component';


@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    RegistroComponent,
    LoginComponent,
    InicioComponent,
    CalendarioComponent,
    PerfilUsuarioComponent,
    MisClasesComponent,
    VistaDeClaseComponent,
    NavbarSecundarioComponent,
    PersonasDeClaseComponent,
    VistaTareaComponent,
    CrearClaseComponent,
    InscribirUsuarioComponent,
    EditarClaseComponent,
    CrearTareaComponent,
    EditarTareaComponent,
    VistaUsersComponent,
    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    HttpClientModule,
    FontAwesomeModule,
    NgxPaginationModule,
    FullCalendarModule,
    
  ],
  providers: [provideRouter(routes,withComponentInputBinding()),provideHttpClient(withInterceptors([jwtInterceptorInterceptor]))],
  bootstrap: [AppComponent]
})
export class AppModule { }
