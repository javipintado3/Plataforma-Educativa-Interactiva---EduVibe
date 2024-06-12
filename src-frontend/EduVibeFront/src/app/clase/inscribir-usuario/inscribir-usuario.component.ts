import { Component, OnInit } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { UserResp } from '../../interfaces/userResp';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';
import { catchError, throwError } from 'rxjs';

@Component({
  selector: 'app-inscribir-usuario',
  templateUrl: './inscribir-usuario.component.html',
  styleUrls: ['./inscribir-usuario.component.css']
})
export class InscribirUsuarioComponent implements OnInit {
  usuarios: UserResp[] = [];
  selectedUser: string = '';
  idClase: number | null = null; // Change to handle null

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
    this.obtenerUsuarios();
  }

  obtenerUsuarios(): void {
    if (this.idClase !== null) {
      this.auth.obtenerUsuariosPorClase(this.idClase).subscribe(
        (usuariosInscritos: UserResp[]) => {
          this.auth.obtenerUsuarios().subscribe(
            (usuarios: UserResp[]) => {
              this.usuarios = usuarios.filter(usuario => {
                const esProfesor = usuario.rol === 'profesor';
                const yaInscrito = usuariosInscritos.some(u => u.email === usuario.email);
                const hayProfesor = usuariosInscritos.some(u => u.rol === 'profesor');
                if (esProfesor && hayProfesor) {
                  return false;
                }
                return !yaInscrito;
              });
            },
            error => {
              console.error('Error al obtener usuarios:', error);
            }
          );
        },
        error => {
          console.error('Error al obtener usuarios inscritos en la clase:', error);
        }
      );
    }
  }

  inscribirUsuario(): void {
    if (this.selectedUser && this.idClase !== null) {
      this.claseService.inscribirUsuario(this.selectedUser, this.idClase)
        .pipe(
          catchError(error => {
            let errorMessage = 'Error al inscribir usuario';
            if (error.status === 409) { // HTTP 409: Conflict
              errorMessage = 'Ya hay un profesor en la clase';
            } else if (error.status === 400) { // HTTP 400: Bad Request
              errorMessage = 'Este alumno ya está inscrito en la clase';
            }
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: errorMessage,
            });
            return throwError(error);
          })
        )
        .subscribe(
          () => {
            Swal.fire({
              icon: 'success',
              title: 'Éxito',
              text: 'Usuario inscrito exitosamente',
            });
            this.router.navigate(['/misClases']);
          },
          error => {
            console.error('Error al inscribir usuario:', error);
          }
        );
    }
  }
}
