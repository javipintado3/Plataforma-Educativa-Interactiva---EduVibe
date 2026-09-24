import { Pipe, PipeTransform } from '@angular/core';

import { environment } from '../../../environments/environment';

/**
 * Completa la URL de un archivo subido (relativa, "/uploads/xxx.png") con el
 * origen del backend.
 *
 * Un enlace pegado a mano ya es absoluto y se deja tal cual; solo hace falta
 * completar lo que ha devuelto nuestro propio endpoint de subida. Se calcula
 * a partir de apiUrl en vez de guardar un origen aparte en environment, para
 * no tener dos valores que sincronizar en cada despliegue.
 */
@Pipe({ name: 'rutaArchivo', standalone: true })
export class RutaArchivoPipe implements PipeTransform {

  private static readonly ORIGEN = environment.apiUrl.replace(/\/api\/?$/, '');

  transform(url: string | null | undefined): string | null {
    if (!url) {
      return null;
    }
    return /^https?:\/\//.test(url) ? url : `${RutaArchivoPipe.ORIGEN}${url}`;
  }
}
