import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DetalleHiloForo, MensajeForo } from '../models';

/**
 * Un hilo del foro ya abierto.
 *
 * Se crea colgado de su clase (ver {@link ClasesService.abrirHilo}); a partir
 * de ahí se consulta, se le responde o se retira por su propio identificador,
 * igual que las tareas cuelgan de {@link TareasService}.
 */
@Injectable({ providedIn: 'root' })
export class ForoService {

  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  detalle(hiloId: string): Observable<DetalleHiloForo> {
    return this.http.get<DetalleHiloForo>(`${this.api}/forum-threads/${hiloId}`);
  }

  responder(hiloId: string, content: string): Observable<MensajeForo> {
    return this.http.post<MensajeForo>(`${this.api}/forum-threads/${hiloId}/posts`, { content });
  }

  borrarHilo(hiloId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/forum-threads/${hiloId}`);
  }

  borrarMensaje(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/forum-posts/${postId}`);
  }
}
