import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { ClaseService } from '../../services/clase.service';
import { ActivatedRoute } from '@angular/router';
import { UserResp } from '../../interfaces/userResp';
import { ClaseDto } from '../../interfaces/clase';

@Component({
  selector: 'app-personas-de-clase',
  templateUrl: './personas-de-clase.component.html',
  styleUrls: ['./personas-de-clase.component.css']
})
export class PersonasDeClaseComponent implements OnInit {
  usuarios: UserResp[] = [];
  profesor: UserResp | null = null;
  alumnos: UserResp[] = [];
  clase: ClaseDto | undefined;
  id: number = 0;

  constructor(
    private authService: AuthService,
    private claseService: ClaseService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = +params['id']; // Convierte el parámetro a número
      this.obtenerClaseYUsuarios(); // Llama a la función para obtener la clase y los usuarios
    });
  }

  obtenerClaseYUsuarios(): void {
    this.claseService.obtenerClasePorId(this.id).subscribe(
      (clase) => {
        console.log('Clase obtenida:', clase);
        this.clase = clase;
        this.obtenerUsuariosPorClase();
      },
      (error) => {
        console.error('Error al obtener la clase:', error);
      }
    );
  }

  obtenerUsuariosPorClase(): void {
    this.authService.obtenerUsuariosPorClase(this.id).subscribe(
      data => {
        this.usuarios = data;
        this.separarProfesorYAlumnos();
      },
      error => {
        console.error('Error al obtener los usuarios de la clase', error);
      }
    );
  }

  private separarProfesorYAlumnos(): void {
    this.usuarios.forEach(usuario => {
      if (usuario.rol === 'profesor') {
        this.profesor = usuario;
      } else {
        this.alumnos.push(usuario);
      }
    });
  }
}
