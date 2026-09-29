import { Entrega } from '../models';

/**
 * Media ponderada, en porcentaje, de las entregas que ya tienen nota; null si
 * ninguna la tiene.
 *
 * Normaliza cada nota a su propia escala antes de pesarla: las tareas de una
 * clase pueden valer puntuaciones distintas (100, 50…) y tener un peso distinto
 * en la nota final, así que sumarlas tal cual mezclaría escalas que no son
 * comparables. Lo que aún no está corregido no cuenta: incluirlo daría un
 * número que baja solo porque el profesor va con retraso.
 *
 * Vive aparte porque la usan a la vez la pestaña de calificaciones de una
 * clase y la sección "Notas", que junta todas las clases.
 */
export function mediaPonderada(entregas: Entrega[]): number | null {
  const calificadas = entregas.filter(entrega => entrega.grade !== null);
  if (!calificadas.length) {
    return null;
  }

  let sumaPonderada = 0;
  let sumaPesos = 0;
  for (const entrega of calificadas) {
    const porcentaje = (Number(entrega.grade!.score) / entrega.points) * 100;
    sumaPonderada += porcentaje * entrega.weight;
    sumaPesos += entrega.weight;
  }
  return Math.round((sumaPonderada / sumaPesos) * 10) / 10;
}
