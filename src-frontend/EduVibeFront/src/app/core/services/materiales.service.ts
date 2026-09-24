import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Material, TipoMaterial } from '../models';

/**
 * Un material ya publicado.
 *
 * Se crea colgado de su clase (ver {@link ClasesService.crearMaterial}); a
 * partir de ahí se edita o se retira por su propio identificador, igual que
 * las tareas cuelgan de {@link TareasService}.
 */
@Injectable({ providedIn: 'root' })
export class MaterialesService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/resources`;

  actualizar(materialId: string, datos: {
    title: string; fileUrl?: string; type?: TipoMaterial | ''; topicId?: string | null;
  }): Observable<Material> {
    return this.http.put<Material>(`${this.api}/${materialId}`, datos);
  }

  eliminar(materialId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${materialId}`);
  }
}
