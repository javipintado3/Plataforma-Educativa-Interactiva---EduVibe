import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClaseService } from '../../services/clase.service';
import { ClaseDto } from '../../interfaces/clase';
import { ActivatedRoute, Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-editar-clase',
  templateUrl: './editar-clase.component.html',
  styleUrls: ['./editar-clase.component.css']
})
export class EditarClaseComponent implements OnInit {

  @Input() idClase: number = 0; // Identificador de la clase a editar, recibido como entrada
  claseForm: FormGroup; // FormGroup para manejar el formulario reactivo
  clase: ClaseDto | null = null; // Objeto para almacenar los datos de la clase

  constructor(
    private fb: FormBuilder, // FormBuilder para construir el FormGroup
    private claseService: ClaseService, // Servicio para manejar las operaciones de las clases
    private route: ActivatedRoute, // ActivatedRoute para obtener parámetros de la ruta
    private router: Router // Router para navegar entre vistas
  ) {
    // Inicialización del FormGroup con los campos necesarios
    this.claseForm = this.fb.group({
      nombre: ['', Validators.required], // Campo 'nombre', requerido
      descripcion: ['', Validators.required] // Campo 'descripcion', requerido
    });
  }

  ngOnInit(): void {
    this.obtenerClase(); // Cargar datos de la clase al iniciar el componente
  }

  obtenerClase(): void {
    // Llamada al servicio para obtener los datos de la clase por ID
    this.claseService.obtenerClasePorId(this.idClase).subscribe({
      next: (clase: ClaseDto) => {
        // Patch value para actualizar el formulario con los datos recibidos
        this.clase = clase;
        this.claseForm.patchValue({
          nombre: clase.nombre,
          descripcion: clase.descripcion
        });
      },
      error: err => {
        // Manejo de errores
        console.log('Error al obtener la clase:', err);
      }
    });
  }

  editarClase(): void {
    if (!this.clase) {
      console.error('La clase no está definida.');
      return;
    }

    this.claseForm.markAllAsTouched(); // Marcar todos los campos como tocados para activar las validaciones
    if (this.claseForm.valid) {
      // Si el formulario es válido, crear un objeto de clase con los datos del formulario
      const clase: ClaseDto = {
        idClase: this.idClase,
        nombre: this.claseForm.get('nombre')?.value,
        descripcion: this.claseForm.get('descripcion')?.value,
      };
      // Llamada al servicio para editar la clase
      this.claseService.editarClase(this.idClase, clase).subscribe({
        next: resp => {
          // Si la edición es exitosa, mostrar una alerta de éxito y redirigir
          Swal.fire({
            title: '¡Éxito!',
            text: 'La clase ha sido editada correctamente.',
            icon: 'success'
          }).then(() => {
            this.router.navigate(['/misClases']);
          });
        },
        error: err => {
          // Manejo de errores en la edición
          console.error('Error al editar la clase:', err);
          Swal.fire({
            title: '¡Error!',
            text: 'Ocurrió un error al intentar editar la clase.',
            icon: 'error'
          });
        }
      });
    } else {
      // Si el formulario no es válido, mostrar una alerta de error
      Swal.fire({
        title: 'Formulario no válido',
        text: 'Por favor, complete el formulario correctamente.',
        icon: 'error'
      });
    }
  }
}
