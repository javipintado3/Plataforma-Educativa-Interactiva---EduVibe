import { Component, OnInit } from '@angular/core';
import { TareaService } from '../../services/tarea.service';
import { UserResp } from '../../interfaces/userResp';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';

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

  constructor(
    private tareaService: TareaService,
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router
  ) {
    const idTarea = this.route.snapshot.paramMap.get('idTarea');
    if (idTarea) {
      this.idTarea = +idTarea;
    }
  }

  ngOnInit(): void {
    this.obtenerUsuarios();
  }

  obtenerUsuarios(): void {
    this.auth.obtenerUsuarios().subscribe(
      (usuarios: UserResp[]) => {
        // Filtrar usuarios para obtener solo aquellos con rol de alumno
        this.alumnos = usuarios.filter(usuario => usuario.rol.includes('alumno'));
      },
      error => {
        console.error('Error al obtener usuarios:', error);
      }
    );
  }

  asignarTareaAUsuario(): void {
    if (this.selectedUser !== null && this.idTarea !== null) {
      this.tareaService.asignarTareaAUsuario(this.idTarea, this.selectedUser).subscribe(
        () => {
          alert('Tarea asignada exitosamente');
          this.router.navigate(['/tareas', this.idTarea]);
        },
        error => {
          console.error('Error al asignar tarea:', error);
        }
      );
    }
  }
}
