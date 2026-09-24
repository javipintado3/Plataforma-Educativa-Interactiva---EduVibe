import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Notificacion } from '../models';

@Injectable({ providedIn: 'root' })
export class NotificacionesService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/notifications`;

  misNotificaciones(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(this.api);
  }

  noLeidas(): Observable<number> {
    return this.http.get<{ count: number }>(`${this.api}/unread-count`)
      .pipe(map(respuesta => respuesta.count));
  }

  marcarLeida(notificacionId: string): Observable<void> {
    return this.http.put<void>(`${this.api}/${notificacionId}/read`, {});
  }

  marcarTodasLeidas(): Observable<void> {
    return this.http.put<void>(`${this.api}/read-all`, {});
  }
}
