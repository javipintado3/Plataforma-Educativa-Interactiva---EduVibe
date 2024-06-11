import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TareaService } from '../../services/tarea.service';
import { TareaDto } from '../../interfaces/tarea';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-editar-tarea',
  templateUrl: './editar-tarea.component.html',
  styleUrls: ['./editar-tarea.component.css']
})
export class EditarTareaComponent implements OnInit {

  @Input() idTarea: number = 0; // Identificador de la tarea a editar, recibido como entrada
  tareaForm: FormGroup; // FormGroup para manejar el formulario reactivo
  tarea: TareaDto | null = null; // Objeto para almacenar los datos de la tarea

  constructor(
    private fb: FormBuilder, // FormBuilder para construir el FormGroup
    private tareaService: TareaService, // Servicio para manejar las operaciones de las tareas
    private route: ActivatedRoute, // ActivatedRoute para obtener parámetros de la ruta
    private router: Router // Router para navegar entre vistas
  ) {
    // Inicialización del FormGroup con los campos necesarios
    this.tareaForm = this.fb.group({
      nombreTarea: ['', Validators.required], // Campo 'nombreTarea', requerido
      enunciado: ['', Validators.required], // Campo 'enunciado', requerido
    });
  }

  ngOnInit(): void {
    this.obtenerTarea(); // Cargar datos de la tarea al iniciar el componente
  }

  obtenerTarea(): void {
    // Llamada al servicio para obtener los datos de la tarea por ID
    this.tareaService.obtenerTareaPorId(this.idTarea).subscribe({
      next: (tarea: TareaDto) => {
        // Patch value para actualizar el formulario con los datos recibidos
        this.tarea = tarea;
        this.tareaForm.patchValue({
          nombreTarea: tarea.nombreTarea,
          enunciado: tarea.enunciado
        });
      },
      error: err => {
        // Manejo de errores
        console.error('Error al obtener la tarea:', err);
      }
    });
  }

  editarTarea(): void {
    if (!this.tarea) {
      console.error('La tarea no está definida.');
      return;
    }

    this.tareaForm.markAllAsTouched(); // Marcar todos los campos como tocados para activar las validaciones
    if (this.tareaForm.valid) {
      // Si el formulario es válido, crear un objeto de tarea con los datos del formulario
      const tarea: TareaDto = {
        idTarea: this.idTarea,
        nombreTarea: this.tareaForm.get('nombreTarea')?.value,
        enunciado: this.tareaForm.get('enunciado')?.value,
        fechaApertura: this.tarea.fechaApertura, // Conservar la fecha de apertura original
        estado: this.tarea.estado, // Conservar el estado original
        calificacion: this.tarea.calificacion, // Conservar la calificación original
        clase: this.tarea.clase, // Conservar la clase original
        archivos: this.tarea.archivos, // Conservar los archivos originales
        user: this.tarea.user
      };
      // Llamada al servicio para editar la tarea
      this.tareaService.editarTarea(this.idTarea, tarea).subscribe({
        next: resp => {
          // Si la edición es exitosa, mostrar una alerta de éxito y redirigir
          Swal.fire({
            title: '¡Éxito!',
            text: 'La tarea ha sido editada correctamente.',
            icon: 'success'
          }).then(() => {
            this.router.navigate(['/tarea', this.idTarea]);
          });
        },
        error: err => {
          // Manejo de errores en la edición
          console.error('Error al editar la tarea:', err);
          Swal.fire({
            title: '¡Error!',
            text: 'Ocurrió un error al intentar editar la tarea.',
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
