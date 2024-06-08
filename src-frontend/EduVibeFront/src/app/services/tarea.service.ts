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
    return this.http.post<any>(`${this.baseUrl}/crear`, tareaDto)

  }

  // Obtener una tarea por su ID
  obtenerTareaPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`)

  }

  // Editar una tarea existente
  editarTarea(id: number, tareaDto: TareaDto): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/editar/${id}`, tareaDto)
   
  }

  // Eliminar una tarea por su ID
  eliminarTarea(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/eliminar/${id}`)
 
  }

  

  // Obtener todas las tareas
  obtenerTodasLasTareas(): Observable<TareaDto[]> {
    return this.http.get<any[]>(`${this.baseUrl}/todos`)

  }

    // Obtener todas las tareas por ID de clase
    obtenerTareasPorClase(idClase: number): Observable<TareaDto[]> {
        return this.http.get<TareaDto[]>(`${this.baseUrl}/clase/user/${idClase}`);
      }

        // Subir un archivo a una tarea
  subirArchivo(idTarea: number, archivo: File): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<any>(`${this.baseUrl}/${idTarea}/archivo`, formData);
  }

  // Descargar un archivo de una tarea
  descargarArchivo(idTarea: number, indice: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${idTarea}/archivo/${indice}`, {
      responseType: 'blob'
    });
  }

      

}
