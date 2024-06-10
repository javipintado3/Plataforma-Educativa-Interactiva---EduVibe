import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserResp } from '../../interfaces/userResp';

@Component({
  selector: 'app-vista-users',
  templateUrl: './vista-users.component.html',
  styleUrl: './vista-users.component.css'
})
export class VistaUsersComponent {
  usuarios: UserResp[] = [];
  p:number = 1

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.authService.obtenerUsuarios().subscribe(
      (usuarios) => {
        this.usuarios = usuarios;
      },
      (error) => {
        console.error('Error al obtener usuarios paginados', error);
      }
    );
  }

  eliminarUser(id:number):void {

  }


}
