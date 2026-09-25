import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DetalleExamen, IntentoExamen, IntentoResumenExamen, PreguntaExamen, ResultadoExamen } from '../models';

/** Un examen ya creado: su detalle, las preguntas (profesorado) y el intento del alumnado. */
@Injectable({ providedIn: 'root' })
export class ExamenesService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/exams`;

  detalle(examId: string): Observable<DetalleExamen> {
    return this.http.get<DetalleExamen>(`${this.api}/${examId}`);
  }

  /** Preguntas con la respuesta correcta marcada. Solo para el profesorado. */
  preguntas(examId: string): Observable<PreguntaExamen[]> {
    return this.http.get<PreguntaExamen[]>(`${this.api}/${examId}/questions`);
  }

  eliminar(examId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${examId}`);
  }

  /** Empieza el examen, o retoma el intento en curso si ya se había empezado. */
  comenzarOReanudar(examId: string): Observable<IntentoExamen> {
    return this.http.post<IntentoExamen>(`${this.api}/${examId}/attempt`, {});
  }

  /** Autoguardado de una respuesta mientras se hace el examen. */
  guardarRespuesta(examId: string, questionId: string, selectedOptionId: string | null): Observable<void> {
    return this.http.put<void>(`${this.api}/${examId}/attempt/answers/${questionId}`, { selectedOptionId });
  }

  /** Entrega el intento; la corrección es automática y llega en la misma respuesta. */
  entregar(examId: string): Observable<ResultadoExamen> {
    return this.http.post<ResultadoExamen>(`${this.api}/${examId}/attempt/submit`, {});
  }

  /** Todos los intentos del examen. Solo para el profesorado de la clase. */
  intentos(examId: string): Observable<IntentoResumenExamen[]> {
    return this.http.get<IntentoResumenExamen[]>(`${this.api}/${examId}/attempts`);
  }
}
