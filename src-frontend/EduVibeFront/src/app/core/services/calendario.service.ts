import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EntradaAgenda, TipoEventoAgenda } from '../models';

/**
 * Agenda del centro: eventos a mano y fechas de entrega, ya mezclados por la API.
 *
 * No cuelga de una clase como {@link ClasesService}: una consulta cubre varias
 * clases a la vez, así que es un dominio propio.
 */
@Injectable({ providedIn: 'root' })
export class CalendarioService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/calendar`;

  agenda(desde: Date, hasta: Date): Observable<EntradaAgenda[]> {
    return this.http.get<EntradaAgenda[]>(this.api, {
      params: { from: desde.toISOString(), to: hasta.toISOString() },
    });
  }

  crear(datos: { title: string; eventDate: string; type?: TipoEventoAgenda | ''; classId?: string | null }): Observable<EntradaAgenda> {
    return this.http.post<EntradaAgenda>(this.api, datos);
  }

  actualizar(eventoId: string, datos: { title: string; eventDate: string; type?: TipoEventoAgenda | '' }): Observable<EntradaAgenda> {
    return this.http.put<EntradaAgenda>(`${this.api}/${eventoId}`, datos);
  }

  eliminar(eventoId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${eventoId}`);
  }
}
