import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ClaseDto } from '../../interfaces/clase';
import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ClaseService } from '../../services/clase.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-crear-tarea',
  templateUrl: './crear-tarea.component.html',
  styleUrls: ['./crear-tarea.component.css']
})
export class CrearTareaComponent implements OnInit {

  tareaForm: FormGroup;
  idClase!: number;
  clase: ClaseDto | null = null;

  constructor(
    private fb: FormBuilder,
    private tareaService: TareaService,
    private router: Router,
    private route: ActivatedRoute,
    private claseService: ClaseService
  ) {
    this.tareaForm = this.fb.group({
      nombreTarea: ['', Validators.required],
      enunciado: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.idClase = +params['idClase'];
      this.claseService.obtenerClasePorId(this.idClase).subscribe(
        (clase: ClaseDto) => {
          this.clase = clase;
        },
        error => {
          console.error('Error al obtener la clase:', error);
          Swal.fire('Error', 'Ocurrió un error al obtener la clase', 'error');
        }
      );
    });
  }

  crearTarea(): void {
    if (this.tareaForm.valid && this.clase) {
      const tarea: TareaDto = {
        nombreTarea: this.tareaForm.get('nombreTarea')!.value,
        enunciado: this.tareaForm.get('enunciado')!.value,
        fechaApertura: new Date(), // Suponiendo que la fecha de apertura se debe establecer en la fecha actual
        estado: false,
        calificacion: 0,
        clase: this.clase,
        archivos: [],
        idTarea: 0
      };

      this.tareaService.crearTareaAsignandoClase(this.idClase, tarea).subscribe({
        next: resp => {
          this.router.navigate(["clase", this.idClase]);

          Swal.fire('Tarea creada', 'La tarea se ha creado correctamente', 'success');
        },
        error: err => {
          console.error('Error al crear la tarea:', err);
          Swal.fire('Error', 'Ocurrió un error al crear la tarea', 'error');
        }
      });
    } else {
      console.error('Formulario no válido. Por favor, complete todos los campos requeridos.');
    }
  }
}
