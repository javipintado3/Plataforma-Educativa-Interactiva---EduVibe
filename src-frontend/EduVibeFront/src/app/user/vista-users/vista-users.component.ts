import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserResp } from '../../interfaces/userResp';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-vista-users',
  templateUrl: './vista-users.component.html',
  styleUrls: ['./vista-users.component.css']
})
export class VistaUsersComponent implements OnInit {
  usuarios: UserResp[] = [];
  usuariosFiltrados: UserResp[] = [];
  searchText: string = '';
  p: number = 1;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.authService.obtenerUsuarios().subscribe(
      (usuarios) => {
        this.usuarios = usuarios;
        this.usuariosFiltrados = usuarios; // Inicialmente mostrar todos los usuarios
      },
      (error) => {
        console.error('Error al obtener usuarios paginados', error);
      }
    );
  }

  buscarUsuario(): void {
    this.usuariosFiltrados = this.usuarios.filter(usuario => 
      usuario.nombre.toLowerCase().includes(this.searchText.toLowerCase()));
  }

  eliminarUser(id: number): void {
    Swal.fire({
      title: '¿Estás seguro?',
      text: 'No podrás revertir esto',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, eliminarlo'
    }).then((result) => {
      if (result.isConfirmed) {
        this.authService.eliminarUsuario(id).subscribe(
          () => {
            Swal.fire(
              'Eliminado',
              'El usuario ha sido eliminado',
              'success'
            );
            this.cargarUsuarios(); // Recargar la lista de usuarios después de eliminar
          },
          (error) => {
            Swal.fire(
              'Error',
              'Ocurrió un error al eliminar el usuario',
              'error'
            );
          }
        );
      }
    });
  }
}
