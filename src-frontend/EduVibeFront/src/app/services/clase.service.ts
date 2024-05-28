// clase.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClaseDto } from '../interfaces/clase';

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
}
