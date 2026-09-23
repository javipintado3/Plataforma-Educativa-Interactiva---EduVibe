import { Component, Input, computed, signal } from '@angular/core';

import { EstadoCuenta, EstadoEntrega, Rol } from '../../core/models';

type Clave = EstadoCuenta | EstadoEntrega | Rol | 'sin-empezar' | 'tarde';

interface Aspecto {
  texto: string;
  clase: string;
}

/**
 * Traduce los valores que devuelve la API a una etiqueta en castellano con su
 * color.
 *
 * Está centralizado aquí y no repartido por las plantillas para que "student"
 * se lea igual en todas las pantallas: si cada vista lo tradujese por su
 * cuenta, acabarían conviviendo "alumno", "Alumno" y "estudiante".
 */
@Component({
  selector: 'app-pastilla-estado',
  standalone: true,
  template: `<span class="pastilla" [class]="'pastilla ' + aspecto().clase">{{ aspecto().texto }}</span>`,
})
export class PastillaEstadoComponent {

  private static readonly ASPECTOS: Record<Clave, Aspecto> = {
    // Cuentas
    pending:  { texto: 'Pendiente',    clase: 'pastilla-ambar' },
    active:   { texto: 'Activa',       clase: 'pastilla-verde' },
    disabled: { texto: 'Desactivada',  clase: 'pastilla-gris' },

    // Roles
    admin:    { texto: 'Administración', clase: 'pastilla-azul' },
    teacher:  { texto: 'Profesorado',    clase: 'pastilla-verde' },
    student:  { texto: 'Alumnado',       clase: 'pastilla-gris' },
    guardian: { texto: 'Tutor legal',    clase: 'pastilla-gris' },

    // Entregas
    draft:        { texto: 'Borrador',    clase: 'pastilla-gris' },
    submitted:    { texto: 'Entregada',   clase: 'pastilla-azul' },
    graded:       { texto: 'Calificada',  clase: 'pastilla-verde' },
    'sin-empezar':{ texto: 'Sin empezar', clase: 'pastilla-gris' },
    tarde:        { texto: 'Fuera de plazo', clase: 'pastilla-roja' },
  };

  private readonly _valor = signal<Clave | null>(null);

  @Input({ required: true })
  set valor(v: Clave | null | undefined) {
    this._valor.set(v ?? null);
  }

  readonly aspecto = computed<Aspecto>(() => {
    const clave = this._valor();
    return (clave && PastillaEstadoComponent.ASPECTOS[clave])
      || { texto: '—', clase: 'pastilla-gris' };
  });
}
