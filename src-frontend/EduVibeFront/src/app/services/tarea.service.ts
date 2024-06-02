import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TareaService {
  private baseUrl = 'http://localhost:9090/tareas'; // URL base de tu backend

  constructor(private http: HttpClient) { }

  // Crear una nueva tarea
  crearTarea(tareaDto: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/crear`, tareaDto)

  }

  // Obtener una tarea por su ID
  obtenerTareaPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`)

  }

  // Editar una tarea existente
  editarTarea(id: number, tareaDto: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/editar/${id}`, tareaDto)
   
  }

  // Eliminar una tarea por su ID
  eliminarTarea(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/eliminar/${id}`)
 
  }

  // Obtener todas las tareas
  obtenerTodasLasTareas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/todos`)

  }

}
