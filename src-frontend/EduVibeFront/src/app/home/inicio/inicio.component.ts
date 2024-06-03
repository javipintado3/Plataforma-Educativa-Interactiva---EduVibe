import { Component, OnInit } from '@angular/core';
import { ClaseDto } from '../../interfaces/clase';
import { ClaseService } from '../../services/clase.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.component.html',
  styleUrls: ['./inicio.component.css']
})
export class InicioComponent implements OnInit {

  clases: ClaseDto[] = [];
  p: number = 1;

  constructor(
    private claseService: ClaseService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    // Obtener el ID del usuario del token almacenado en el localStorage
    const id = this.authService.getUserId();

    // Verificar si el ID del usuario está disponible
    if (id) {
      // Llamar al servicio para obtener las clases del usuario
      this.obtenerClasesPorUsuario(id);
    } else {
      console.error('No se pudo obtener el ID del usuario.');
    }
  }

  obtenerClasesPorUsuario(id: number): void {
    // Llamar al servicio para obtener las clases del usuario por su ID
    this.claseService.obtenerClasesPorUsuario(id).subscribe(
      clases => {
        this.clases = clases;
      },
      error => {
        console.error('Error al obtener las clases del usuario:', error);
      }
    );
  }
}
