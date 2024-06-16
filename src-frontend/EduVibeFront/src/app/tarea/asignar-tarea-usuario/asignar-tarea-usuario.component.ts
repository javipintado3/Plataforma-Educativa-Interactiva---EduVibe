import { Component, OnInit } from '@angular/core';
import { TareaService } from '../../services/tarea.service';
import { ClaseService } from '../../services/clase.service';
import { UserResp } from '../../interfaces/userResp';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-asignar-tarea-usuario',
  templateUrl: './asignar-tarea-usuario.component.html',
  styleUrls: ['./asignar-tarea-usuario.component.css']
})
export class AsignarTareaUsuarioComponent implements OnInit {
  usuarios: UserResp[] = [];
  alumnos: UserResp[] = [];
  selectedUser: number | null = null;
  idTarea: number | null = null;
  idClase: number | null = null;

  constructor(
    private tareaService: TareaService,
    private claseService: ClaseService,
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router
  ) {
    const idTarea = this.route.snapshot.paramMap.get('idTarea');
    if (idTarea) {
      this.idTarea = +idTarea;
    }
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
          this.alumnos = usuarios.filter(usuario => usuario.rol.includes('alumno'));
        },
        error => {
          console.error('Error al obtener usuarios de la clase:', error);
        }
      );
    }
  }

  asignarTareaAUsuario(): void {
    if (this.selectedUser !== null && this.idTarea !== null) {
      this.tareaService.asignarTareaAUsuario(this.idTarea, this.selectedUser).subscribe(
        () => {
          Swal.fire({
            icon: 'success',
            title: 'Éxito',
            text: 'Tarea asignada exitosamente',
          });
          this.router.navigate(['/tarea', this.idTarea]);
        },
        error => {
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Error al asignar tarea',
          });
          console.error('Error al asignar tarea:', error);
        }
      );
    } else {
      Swal.fire({
        icon: 'warning',
        title: 'Advertencia',
        text: 'Por favor, seleccione un usuario',
      });
    }
  }
}
