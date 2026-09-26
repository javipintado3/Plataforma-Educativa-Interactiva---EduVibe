import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Subgrupo } from '../models';

/**
 * Un subgrupo ya creado.
 *
 * Se crea colgado de su clase (ver {@link ClasesService.crearSubgrupo}); a
 * partir de ahí se edita entero o se retira por su propio identificador,
 * igual que las tareas cuelgan de {@link TareasService}.
 */
@Injectable({ providedIn: 'root' })
export class SubgruposService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/class-groups`;

  actualizar(groupId: string, datos: { name: string; memberIds: string[] }): Observable<Subgrupo> {
    return this.http.put<Subgrupo>(`${this.api}/${groupId}`, datos);
  }

  eliminar(groupId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${groupId}`);
  }
}
