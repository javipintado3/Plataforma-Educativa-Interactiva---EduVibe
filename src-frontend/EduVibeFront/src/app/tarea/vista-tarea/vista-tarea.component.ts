import { Component, OnInit } from '@angular/core';
import { TareaDto } from '../../interfaces/tarea';
import { TareaService } from '../../services/tarea.service';
import { ActivatedRoute } from '@angular/router';
import { saveAs } from 'file-saver';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';
import { ClaseDto } from '../../interfaces/clase';

@Component({
  selector: 'app-vista-tarea',
  templateUrl: './vista-tarea.component.html',
  styleUrls: ['./vista-tarea.component.css']
})
export class VistaTareaComponent implements OnInit {
  tarea: TareaDto | undefined;
  id: number = 0;
  archivo: File | null = null;
  nuevaCalificacion: number | null = null;

  solucionEscrita: string = ''; // Definición de la propiedad solucionEscrita

  constructor(
    private tareaService: TareaService,
    private route: ActivatedRoute,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = +params['id']; // Convierte el parámetro a número
      this.obtenerTarea(); // Llama a la función para obtener la tarea
    });
  }

  obtenerTarea(): void {
    this.tareaService.obtenerTareaPorId(this.id).subscribe(
      (tarea) => {
        this.tarea = tarea;
      },
      (error) => {
        console.error('Error al obtener la tarea:', error);
      }
    );
  }

  onArchivoSeleccionado(event: any): void {
    this.archivo = event.target.files[0];
  }

  subirArchivo(): void {
    if (this.tarea && this.archivo) {
      this.tareaService.subirArchivo(this.tarea.idTarea, this.archivo).subscribe(
        () => {
          Swal.fire('Éxito', 'Archivo subido exitosamente', 'success');
          this.obtenerTarea(); // Recargar la tarea para obtener la lista actualizada de archivos
        },
        (error) => {
          console.error('Error al subir el archivo:', error);
        }
      );
    }
  }

  descargarArchivo(indice: number): void {
    if (this.tarea) {
      this.tareaService.descargarArchivo(this.tarea.idTarea, indice).subscribe(blob => {
        const archivoNombre = `archivo_${indice}.pdf`; // Nombre del archivo descargado
        saveAs(blob, archivoNombre);
      });
    }
  }

  editarCalificacion(idTarea: number): void {
    if (this.nuevaCalificacion !== null && this.nuevaCalificacion >= 0 && this.nuevaCalificacion <= 10) {
      this.tareaService.editarCalificacion(idTarea, this.nuevaCalificacion).subscribe(
        () => {
          Swal.fire('Éxito', 'Calificación actualizada exitosamente', 'success');
          this.obtenerTarea(); // Recargar la tarea para obtener la calificación actualizada
        },
        (error) => {
          console.error('Error al actualizar la calificación:', error);
        }
      );
    } else {
      Swal.fire('Error', 'Por favor, ingrese una calificación válida entre 0 y 10.', 'error');
    }
  }

  enviarSolucionEscrita(): void {
    if (this.tarea && this.solucionEscrita.trim() !== '') {
      this.tareaService.editarSolucionEscrita(this.tarea.idTarea, this.solucionEscrita).subscribe(
        () => {
          Swal.fire('Éxito', 'Solución escrita enviada exitosamente', 'success');
          if (this.tarea) {
            this.tarea.solucionEscrita = this.solucionEscrita;
            this.tarea.estado = true; // Cambiar el estado a "completado"
          }
        },
        (error) => {
          console.error('Error al enviar la solución escrita:', error);
        }
      );
    } else {
      Swal.fire('Error', 'Por favor, ingrese una solución escrita válida.', 'error');
    }
  }
}
