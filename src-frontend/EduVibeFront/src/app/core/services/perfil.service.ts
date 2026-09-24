import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ResumenPerfil, Usuario } from '../models';

@Injectable({ providedIn: 'root' })
export class PerfilService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/profile`;

  resumen(): Observable<ResumenPerfil> {
    return this.http.get<ResumenPerfil>(`${this.api}/summary`);
  }

  /** "" (o sin argumento) quita la foto: se vuelve a las iniciales. */
  actualizarAvatar(url = ''): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.api}/avatar`, { url });
  }
}
