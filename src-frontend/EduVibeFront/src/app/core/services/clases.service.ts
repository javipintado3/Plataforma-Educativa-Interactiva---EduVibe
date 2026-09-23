import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Clase, DetalleClase, DetalleTarea, Entrega, Miembro, Tarea, Tema } from '../models';

/**
 * Clases y todo lo que cuelga de una.
 *
 * Los servicios no guardan estado: devuelven el observable y quien llama
 * decide qué hacer con él. El único estado compartido de la aplicación es la
 * sesión, y vive en AuthService.
 */
@Injectable({ providedIn: 'root' })
export class ClasesService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/classes`;

  /** Las clases de quien consulta; para la administración, todas las del centro. */
  misClases(): Observable<Clase[]> {
    return this.http.get<Clase[]>(this.api);
  }

  detalle(claseId: string): Observable<DetalleClase> {
    return this.http.get<DetalleClase>(`${this.api}/${claseId}`);
  }

  crear(datos: { name: string; subject?: string; color?: string }): Observable<DetalleClase> {
    return this.http.post<DetalleClase>(this.api, datos);
  }

  actualizar(claseId: string, datos: { name: string; subject?: string; color?: string }): Observable<DetalleClase> {
    return this.http.put<DetalleClase>(`${this.api}/${claseId}`, datos);
  }

  miembros(claseId: string): Observable<Miembro[]> {
    return this.http.get<Miembro[]>(`${this.api}/${claseId}/members`);
  }

  matricular(claseId: string, userId: string, roleInClass: 'teacher' | 'student'): Observable<Miembro> {
    return this.http.post<Miembro>(`${this.api}/${claseId}/members`, { userId, roleInClass });
  }

  desmatricular(claseId: string, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${claseId}/members/${userId}`);
  }

  temas(claseId: string): Observable<Tema[]> {
    return this.http.get<Tema[]>(`${this.api}/${claseId}/topics`);
  }

  crearTema(claseId: string, title: string): Observable<Tema> {
    return this.http.post<Tema>(`${this.api}/${claseId}/topics`, { title });
  }

  tareas(claseId: string): Observable<Tarea[]> {
    return this.http.get<Tarea[]>(`${this.api}/${claseId}/assignments`);
  }

  crearTarea(claseId: string, datos: {
    title: string; description?: string; dueDate?: string | null; points?: number; topicId?: string | null;
  }): Observable<DetalleTarea> {
    return this.http.post<DetalleTarea>(`${this.api}/${claseId}/assignments`, datos);
  }

  /** Las entregas propias en esta clase: la pestaña de calificaciones. */
  misEntregas(claseId: string): Observable<Entrega[]> {
    return this.http.get<Entrega[]>(`${this.api}/${claseId}/my-submissions`);
  }
}
