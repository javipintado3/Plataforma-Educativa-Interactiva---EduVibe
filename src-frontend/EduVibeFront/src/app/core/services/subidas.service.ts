import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';

export type TipoSubida = 'imagen' | 'documento';

/**
 * Sube un archivo suelto y devuelve la ruta con la que guardarlo como
 * imageUrl o fileUrl del recurso que corresponda (clase, material, entrega).
 *
 * No sabe para qué se usa el archivo: eso lo decide quien llama, mandando
 * después esa misma URL al endpoint de la clase, el material o la entrega.
 */
@Injectable({ providedIn: 'root' })
export class SubidasService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/uploads`;

  subir(archivo: File, tipo: TipoSubida = 'documento'): Observable<string> {
    const datos = new FormData();
    datos.append('file', archivo);
    datos.append('tipo', tipo);

    return this.http.post<{ url: string }>(this.api, datos).pipe(map(respuesta => respuesta.url));
  }
}
