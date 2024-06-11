import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TareaDto } from '../interfaces/tarea';

@Injectable({
  providedIn: 'root'
})
export class TareaService {
  private baseUrl = 'http://localhost:9090/tareas'; // URL base de tu backend

  constructor(private http: HttpClient) { }

  // Crear una nueva tarea
  crearTarea(tareaDto: TareaDto): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/crear`, tareaDto);
  }

    // Crear una nueva tarea asignando una clase
    crearTareaAsignandoClase(idClase: number, tareaDto: TareaDto): Observable<number> {
      return this.http.post<number>(`${this.baseUrl}/crear/${idClase}`, tareaDto);
    }

  // Obtener una tarea por su ID
  obtenerTareaPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  // Editar una tarea existente
  editarTarea(id: number, tareaDto: TareaDto): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/editar/${id}`, tareaDto);
  }

  // Eliminar una tarea por su ID
  eliminarTarea(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/eliminar/${id}`);
  }

  // Obtener todas las tareas
  obtenerTodasLasTareas(): Observable<TareaDto[]> {
    return this.http.get<any[]>(`${this.baseUrl}/todos`);
  }

  // Obtener todas las tareas por ID de clase
  obtenerTareasPorClase(idClase: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/clase/${idClase}`);
  }
  subirArchivo(idTarea: number, archivo: File): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<any>(`${this.baseUrl}/${idTarea}/archivo`, formData);
  }

  descargarArchivo(idTarea: number, indice: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${idTarea}/archivo/${indice}`, {
      responseType: 'blob'
    });
  }

  obtenerMediaCalificaciones(idClase: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/mediaCalificaciones/${idClase}`);
  }

    // Editar la calificación de una tarea
    editarCalificacion(id: number, nuevaCalificacion: number): Observable<void> {
      return this.http.put<void>(`${this.baseUrl}/calificacion/${id}?nuevaCalificacion=${nuevaCalificacion}`, null);
    }

    // Obtener todas las tareas por ID de clase para el usuario actual
    obtenerTareasPorClaseParaUsuarioActual(idClase: number): Observable<any> {
      return this.http.get<any>(`${this.baseUrl}/clase/user/${idClase}`);
    }

    asignarTareaAUsuario(idTarea: number, idUsuario: number): Observable<void> {
      const url = `${this.baseUrl}/asignar?idTarea=${idTarea}&idUsuario=${idUsuario}`;
      return this.http.post<void>(url, null);
    }
}
