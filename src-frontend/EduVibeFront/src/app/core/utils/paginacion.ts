import { computed, signal } from '@angular/core';
import { EMPTY, Observable, expand, reduce } from 'rxjs';

import { Pagina } from '../models';

/** Elementos por página en toda la aplicación. La API usa el mismo valor por defecto. */
export const TAMANO_PAGINA = 10;

/** Tope de elementos por página que admite la API (`spring.data.web.pageable.max-page-size`). */
export const TAMANO_MAXIMO_PAGINA = 50;

export function totalDePaginas(total: number, tamano = TAMANO_PAGINA): number {
  return Math.max(1, Math.ceil(total / tamano));
}

/** El trozo de una lista que corresponde a una página (desde 0), recortando la página si se pasa. */
export function trozo<T>(elementos: readonly T[], pagina: number, tamano = TAMANO_PAGINA): T[] {
  const valida = Math.min(Math.max(0, pagina), totalDePaginas(elementos.length, tamano) - 1);
  return elementos.slice(valida * tamano, (valida + 1) * tamano);
}

/**
 * Pagina una lista de bloques (elementos agrupados, p. ej. por unidad) contando
 * elementos y no bloques: diez tareas por página, sea cual sea su reparto entre
 * unidades. Un bloque cortado entre dos páginas aparece en las dos, con la parte
 * que le toca en cada una.
 */
export function paginarBloques<B, E>(
  bloques: readonly B[],
  elementosDe: (bloque: B) => readonly E[],
  conElementos: (bloque: B, elementos: E[]) => B,
  pagina: number,
  tamano = TAMANO_PAGINA,
): B[] {
  const inicio = pagina * tamano;
  const fin = inicio + tamano;
  const resultado: B[] = [];
  let vistos = 0;

  for (const bloque of bloques) {
    const elementos = elementosDe(bloque);
    const desde = Math.max(inicio - vistos, 0);
    const hasta = Math.min(fin - vistos, elementos.length);
    if (hasta > desde) {
      resultado.push(conElementos(bloque, elementos.slice(desde, hasta)));
    }
    vistos += elementos.length;
  }
  return resultado;
}

/**
 * Paginación en el cliente, para listas que ya están cargadas enteras.
 *
 * Se usa cuando el conjunto completo hace falta para otra cosa —la media
 * ponderada de las notas, el filtro por unidad, un contador— y por tanto no
 * se puede pedir al servidor de diez en diez. La página pedida se recorta sola
 * si la lista se acorta (al filtrar o borrar), sin tener que corregirla a mano.
 *
 * {@code origen} es una función, y no la lista misma, para que la paginación se
 * recalcule sola cuando cambia la señal de la que sale.
 */
export function paginacionLocal<T>(origen: () => readonly T[], tamano = TAMANO_PAGINA) {
  const solicitada = signal(0);
  const total = computed(() => origen().length);
  const totalPaginas = computed(() => totalDePaginas(total(), tamano));
  const pagina = computed(() => Math.min(solicitada(), totalPaginas() - 1));
  const visibles = computed(() => origen().slice(pagina() * tamano, (pagina() + 1) * tamano));

  return {
    pagina,
    total,
    totalPaginas,
    visibles,
    irA: (destino: number) => solicitada.set(destino),
    reiniciar: () => solicitada.set(0),
  };
}

export type PaginacionLocal<T> = ReturnType<typeof paginacionLocal<T>>;

/**
 * Recorre todas las páginas de un listado paginado en el servidor y junta el
 * contenido. Para quien necesita el conjunto entero (el calendario, la sección
 * de notas...) sin que la API tenga que devolverlo de golpe.
 */
export function todasLasPaginas<T>(pedir: (pagina: number) => Observable<Pagina<T>>): Observable<T[]> {
  return pedir(0).pipe(
    expand(actual => actual.ultima ? EMPTY : pedir(actual.pagina + 1)),
    reduce((acumulado, actual) => acumulado.concat(actual.contenido), [] as T[]),
  );
}
