import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgClass, NgFor, NgIf } from '@angular/common';

import { ClasesService } from '../../../../core/services/clases.service';
import { AnaliticaAlumno, AnaliticaClase, PuntoNota } from '../../../../core/models';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';
import { FechaPipe } from '../../../../shared/pipes/fecha.pipe';
import { PaginadorComponent } from '../../../../shared/paginador/paginador.component';
import { paginacionLocal } from '../../../../core/utils/paginacion';

/** Un punto ya convertido a coordenadas SVG, para pintar la gráfica de evolución. */
interface PuntoGrafica extends PuntoNota {
  x: number;
  y: number;
}

const ALTO_GRAFICA = 110;
const ANCHO_GRAFICA = 300;
const MARGEN = 20;

/**
 * Pestaña "Analítica" (solo profesorado/administración): quién ha entregado,
 * quién no, y quién acumula tareas vencidas sin entregar.
 *
 * Todo llega ya calculado del backend; aquí solo se pinta, salvo la
 * conversión de los puntos de nota a coordenadas SVG para la gráfica de
 * evolución de cada alumno, que solo hace falta cuando se despliega su fila.
 */
@Component({
  selector: 'app-pestana-analitica',
  standalone: true,
  imports: [
    NgIf, NgFor, NgClass,
    CargandoComponent, EstadoVacioComponent, AvisoComponent, FechaPipe,
    PaginadorComponent,
  ],
  templateUrl: './pestana-analitica.component.html',
  styleUrl: './pestana-analitica.component.css',
})
export class PestanaAnaliticaComponent implements OnInit {

  /** Diez alumnos por página; los totales de arriba (media, en riesgo) siguen contando a toda la clase. */
  readonly paginacionAlumnos = paginacionLocal(() => this.analitica()?.students ?? []);

  private readonly clasesService = inject(ClasesService);

  @Input({ required: true }) claseId!: string;

  readonly analitica = signal<AnaliticaClase | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly desplegado = signal<string | null>(null);

  ngOnInit(): void {
    this.clasesService.analitica(this.claseId).subscribe({
      next: (analitica) => {
        this.analitica.set(analitica);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar la analítica'));
        this.cargando.set(false);
      },
    });
  }

  alternar(userId: string): void {
    this.desplegado.set(this.desplegado() === userId ? null : userId);
  }

  /** Convierte el % de cada nota en coordenadas dentro del viewBox de la gráfica. */
  puntosDe(alumno: AnaliticaAlumno): PuntoGrafica[] {
    const notas = alumno.grades;
    const paso = notas.length > 1 ? (ANCHO_GRAFICA - 2 * MARGEN) / (notas.length - 1) : 0;

    return notas.map((nota, indice) => ({
      ...nota,
      x: MARGEN + indice * paso,
      y: ALTO_GRAFICA - (nota.percent / 100) * (ALTO_GRAFICA - 20) - 10,
    }));
  }

  puntosSvg(alumno: AnaliticaAlumno): string {
    return this.puntosDe(alumno).map(p => `${p.x},${p.y}`).join(' ');
  }

  readonly anchoGrafica = ANCHO_GRAFICA;
  readonly altoGrafica = ALTO_GRAFICA;
}
