import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';
import { AuthService } from '../../services/auth.service';
import { ClaseService } from '../../services/clase.service';
import { UserResp } from '../../interfaces/userResp';

@Component({
  selector: 'app-inscribir-usuario',
  templateUrl: './inscribir-usuario.component.html',
  styleUrls: ['./inscribir-usuario.component.css']
})
export class InscribirUsuarioComponent implements OnInit {
  usuarios: UserResp[] = [];
  selectedUser: number | null = null;
  idClase: number | null = null;
  profesorInscrito: boolean = false;

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
          console.log('Usuarios inscritos:', usuariosInscritos);
          // Verificar si ya hay un profesor inscrito
          this.profesorInscrito = usuariosInscritos.some(u => u.rol === 'profesor');
          
          this.auth.obtenerUsuarios().subscribe(
            (todosUsuarios: UserResp[]) => {
              console.log('Todos los usuarios:', todosUsuarios);
              if (usuariosInscritos && todosUsuarios) {
                this.usuarios = todosUsuarios.filter(usuario => {
                  return !usuariosInscritos.some(u => u.id === usuario.id) && (usuario.rol !== 'profesor' || !this.profesorInscrito);
                });
              } else {
                this.usuarios = todosUsuarios.filter(usuario => usuario.rol !== 'profesor' || !this.profesorInscrito);
              }
              console.log('Usuarios no inscritos:', this.usuarios);
            },
            error => {
              console.error('Error al obtener todos los usuarios:', error);
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
    if (this.selectedUser !== null && this.idClase !== null) {
      this.claseService.inscribirUsuario(this.selectedUser, this.idClase)
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
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: 'Hubo un error al inscribir al usuario. Por favor, inténtalo nuevamente.',
            });
          }
        );
    } else {
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: 'Por favor selecciona un usuario y asegúrate de que la clase esté definida.',
      });
    }
  }
}
