import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DetalleTarea, Entrega } from '../models';

/** Tareas, entregas y corrección. */
@Injectable({ providedIn: 'root' })
export class TareasService {

  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  detalle(tareaId: string): Observable<DetalleTarea> {
    return this.http.get<DetalleTarea>(`${this.api}/assignments/${tareaId}`);
  }

  actualizar(tareaId: string, datos: {
    title: string; description?: string; dueDate?: string | null; points?: number; topicId?: string | null;
  }): Observable<DetalleTarea> {
    return this.http.put<DetalleTarea>(`${this.api}/assignments/${tareaId}`, datos);
  }

  eliminar(tareaId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/assignments/${tareaId}`);
  }

  /**
   * Guarda o envía la entrega propia.
   *
   * Un único método para las dos cosas, igual que en la API: es el mismo gesto
   * con la casilla `enviar` puesta o no.
   */
  guardarMiEntrega(tareaId: string, datos: {
    content?: string; fileUrl?: string; enviar?: boolean;
  }): Observable<Entrega> {
    return this.http.put<Entrega>(`${this.api}/assignments/${tareaId}/submission`, datos);
  }

  /** Todas las entregas de la tarea. Solo para el profesorado de la clase. */
  entregas(tareaId: string): Observable<Entrega[]> {
    return this.http.get<Entrega[]>(`${this.api}/assignments/${tareaId}/submissions`);
  }

  calificar(entregaId: string, score: number, feedback?: string): Observable<Entrega> {
    return this.http.put<Entrega>(`${this.api}/submissions/${entregaId}/grade`, { score, feedback });
  }
}
