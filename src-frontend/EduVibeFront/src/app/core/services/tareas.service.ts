import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DetalleTarea, Entrega, EntregaPorCorregir, Rubrica } from '../models';

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

  /** Entregas sin corregir de todas las clases del profesorado, las más antiguas primero. */
  porCorregir(): Observable<EntregaPorCorregir[]> {
    return this.http.get<EntregaPorCorregir[]>(`${this.api}/submissions/pending-review`);
  }

  /**
   * Califica una entrega. Con `rubricScores`, `score` se ignora en el servidor:
   * la nota es la suma de lo puntuado en cada criterio.
   */
  calificar(entregaId: string, score: number | null, feedback?: string,
            rubricScores?: { criterionId: string; points: number }[]): Observable<Entrega> {
    return this.http.put<Entrega>(`${this.api}/submissions/${entregaId}/grade`, { score, feedback, rubricScores });
  }

  /** Nota rápida sobre la entrega, sin calificarla: p.ej. "revisa este apartado". */
  comentar(entregaId: string, teacherNote: string): Observable<Entrega> {
    return this.http.put<Entrega>(`${this.api}/submissions/${entregaId}/comment`, { teacherNote });
  }

  /** Crea la rúbrica de la tarea, o sustituye por completo la que ya hubiera. */
  guardarRubrica(tareaId: string, criteria: { description: string; maxPoints: number }[]): Observable<Rubrica> {
    return this.http.put<Rubrica>(`${this.api}/assignments/${tareaId}/rubric`, { criteria });
  }

  borrarRubrica(tareaId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/assignments/${tareaId}/rubric`);
  }
}
