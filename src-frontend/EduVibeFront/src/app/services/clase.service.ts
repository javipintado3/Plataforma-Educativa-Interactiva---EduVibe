import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { ClaseDto } from '../interfaces/clase';
import { UserResp } from '../interfaces/userResp';

@Injectable({
  providedIn: 'root'
})
export class ClaseService {
  private baseUrl = 'http://localhost:9090/clases'; // URL base de tu backend

  constructor(private http: HttpClient) { }

  // Crear una nueva clase
  crearClase(clase: ClaseDto): Observable<ClaseDto> {
    return this.http.post<ClaseDto>(`${this.baseUrl}/crear`, clase);
  }

  // Obtener una clase por su ID
  obtenerClasePorId(id: number): Observable<ClaseDto> {
    return this.http.get<ClaseDto>(`${this.baseUrl}/${id}`);
  }

  // Editar una clase existente
  editarClase(id: number, clase: ClaseDto): Observable<ClaseDto> {
    return this.http.put<ClaseDto>(`${this.baseUrl}/editar/${id}`, clase);
  }

  // Eliminar una clase por su ID
  eliminarClase(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/eliminar/${id}`);
  }
  
  // Obtener todas las clases
  obtenerTodasLasClases(): Observable<ClaseDto[]> {
    return this.http.get<ClaseDto[]>(`${this.baseUrl}/todos`);
  }

  // Obtener clases por usuario
  obtenerClasesPorUsuario(idUsuario: number): Observable<ClaseDto[]> {
    return this.http.get<ClaseDto[]>(`${this.baseUrl}/usuario/${idUsuario}`);
  }
  eliminarUsuarioDeClase(email: string, idClase: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${idClase}/eliminar-usuario`, { params: { email } }).pipe(
      catchError(error => {
        console.error('Error al eliminar usuario de la clase', error);
        return throwError(error);
      })
    );
  }
  
  // Método para inscribir un usuario en una clase
  inscribirUsuario(idUsuario: number, idClase: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/inscribir`, null, { params: { idUsuario: idUsuario, idClase: idClase } });
  }


}
