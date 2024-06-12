import { Component, OnInit } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { UserResp } from '../../interfaces/userResp';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';
import { catchError, throwError } from 'rxjs';

@Component({
  selector: 'app-designar-usuario-de-clase',
  templateUrl: './designar-usuario-de-clase.component.html',
  styleUrls: ['./designar-usuario-de-clase.component.css']
})
export class DesignarUsuarioDeClaseComponent implements OnInit {
  usuarios: UserResp[] = [];
  selectedUser: string = '';
  idClase: number | null = null;

  constructor(
    private claseService: ClaseService,
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router
  ) {
    const idClase = this.route.snapshot.paramMap.get('idClase');
    if (idClase) {
      this.idClase = +idClase;
    }
  }

  ngOnInit(): void {
    this.obtenerUsuariosDeClase();
  }

  obtenerUsuariosDeClase(): void {
    if (this.idClase !== null) {
      this.auth.obtenerUsuariosDeClase(this.idClase).subscribe(
        (usuarios: UserResp[]) => {
          this.usuarios = usuarios;
        },
        error => {
          console.error('Error al obtener usuarios de la clase:', error);
        }
      );
    }
  }

  desinscribirUsuario(): void {
    if (this.selectedUser && this.idClase !== null) {
      this.claseService.eliminarUsuarioDeClase(this.selectedUser, this.idClase)
        .pipe(
          catchError(error => {
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'El usuario no está en la clase',
            });
            return throwError(error);
          })
        )
        .subscribe(
          () => {
            Swal.fire({
              icon: 'success',
              title: 'Éxito',
              text: 'Usuario eliminado exitosamente',
            });
            this.router.navigate(['/misClases']);
          },
          error => {
            console.error('Error al eliminar usuario:', error);
          }
        );
    }
  }
}
