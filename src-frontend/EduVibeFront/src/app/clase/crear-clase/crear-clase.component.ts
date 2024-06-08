import { Component } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { Router } from '@angular/router';
import { ClaseDto } from '../../interfaces/clase';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-crear-clase',
  templateUrl: './crear-clase.component.html',
  styleUrls: ['./crear-clase.component.css']
})
export class CrearClaseComponent {
  clase: ClaseDto = {
    nombre: '',
    descripcion: '',
    idClase: 0
  };

  constructor(private claseService: ClaseService, private router: Router) { }

  crearClase(): void {
    this.claseService.crearClase(this.clase).subscribe(
      response => {
        Swal.fire('¡Éxito!', 'Clase creada exitosamente')
          .then(() => {
            // Use response.idClase to navigate to the inscribir-usuario component
            this.router.navigate(['/inscribir-usuario', response.idClase]);
          });
      },
      error => {
        Swal.fire('¡Error!', 'Error al crear la clase');
        console.error('Error al crear la clase:', error);
      }
    );
  }
}
