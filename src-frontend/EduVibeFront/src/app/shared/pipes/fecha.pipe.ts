import { Pipe, PipeTransform } from '@angular/core';

/**
 * Fecha en castellano y en formato corto: "12 oct, 18:30".
 *
 * Se escribe una vez aquí en lugar de repartir DatePipe con el mismo patrón y
 * el mismo locale por quince plantillas, donde antes o después una se escribe
 * distinta.
 */
@Pipe({ name: 'fecha', standalone: true })
export class FechaPipe implements PipeTransform {

  private static readonly MESES = [
    'ene', 'feb', 'mar', 'abr', 'may', 'jun',
    'jul', 'ago', 'sep', 'oct', 'nov', 'dic'
  ];

  transform(valor: string | null | undefined, conHora = true): string {
    if (!valor) {
      return '—';
    }

    const fecha = new Date(valor);
    if (isNaN(fecha.getTime())) {
      return '—';
    }

    const dia = fecha.getDate();
    const mes = FechaPipe.MESES[fecha.getMonth()];
    const anio = fecha.getFullYear();
    const anioActual = new Date().getFullYear();

    // El año solo se muestra si no es el actual: en el 90 % de los casos
    // sobra y solo añade ruido
    const base = anio === anioActual ? `${dia} ${mes}` : `${dia} ${mes} ${anio}`;

    if (!conHora) {
      return base;
    }

    const hora = String(fecha.getHours()).padStart(2, '0');
    const minuto = String(fecha.getMinutes()).padStart(2, '0');
    return `${base}, ${hora}:${minuto}`;
  }
}

/**
 * Plazo en lenguaje natural: "Vence mañana", "Venció hace 3 días".
 *
 * Es lo que hace útil la línea de estado de una tarjeta de clase: una fecha
 * suelta obliga a calcular mentalmente si queda tiempo o no.
 */
@Pipe({ name: 'plazo', standalone: true })
export class PlazoPipe implements PipeTransform {

  transform(valor: string | null | undefined): string {
    if (!valor) {
      return 'Sin fecha límite';
    }

    const fecha = new Date(valor);
    if (isNaN(fecha.getTime())) {
      return 'Sin fecha límite';
    }

    const milisegundosPorDia = 86_400_000;
    const hoy = new Date();

    // Se comparan días de calendario, no franjas de 24 horas: algo que vence
    // esta noche a las 23:00 vence "hoy", no "en 0 días"
    const inicioHoy = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate()).getTime();
    const inicioFecha = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate()).getTime();
    const dias = Math.round((inicioFecha - inicioHoy) / milisegundosPorDia);

    if (dias === 0) return 'Vence hoy';
    if (dias === 1) return 'Vence mañana';
    if (dias === -1) return 'Venció ayer';
    if (dias > 1 && dias <= 14) return `Vence en ${dias} días`;
    if (dias < -1 && dias >= -14) return `Venció hace ${Math.abs(dias)} días`;

    return dias > 0 ? 'Vence el ' + this.corta(fecha) : 'Venció el ' + this.corta(fecha);
  }

  private corta(fecha: Date): string {
    return `${fecha.getDate()} ${FechaPipe['MESES'][fecha.getMonth()]}`;
  }
}
