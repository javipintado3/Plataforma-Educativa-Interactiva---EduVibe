import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { ClasesService } from '../../../core/services/clases.service';
import { DetalleClase } from '../../../core/models';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { PestanaCalificacionesComponent } from './pestanas/pestana-calificaciones.component';
import { PestanaPersonasComponent } from './pestanas/pestana-personas.component';
import { PestanaTrabajoComponent } from './pestanas/pestana-trabajo.component';

type Pestana = 'trabajo' | 'personas' | 'calificaciones';

/**
 * Pantalla de una clase.
 *
 * Es solo la cabecera y el conmutador de pestañas; cada pestaña es un
 * componente aparte que pide sus propios datos. Así esta clase no acumula la
 * lógica de las tres, y abrir la pantalla no dispara tres peticiones de las
 * que dos no se van a ver.
 */
@Component({
  selector: 'app-detalle-clase',
  standalone: true,
  imports: [
    NgIf, RouterLink, CargandoComponent, AvisoComponent,
    PestanaTrabajoComponent, PestanaPersonasComponent, PestanaCalificacionesComponent,
  ],
  templateUrl: './detalle-clase.component.html',
  styleUrl: './detalle-clase.component.css',
})
export class DetalleClaseComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  readonly auth = inject(AuthService);

  /** Llega de la ruta /clases/:id gracias a withComponentInputBinding(). */
  @Input() id = '';

  readonly clase = signal<DetalleClase | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly pestana = signal<Pestana>('trabajo');

  /**
   * La pestaña de calificaciones muestra las entregas propias, así que solo
   * tiene sentido para quien las tiene: el alumnado de la clase.
   */
  readonly muestraCalificaciones = computed(() => this.clase()?.miRol === 'student');

  ngOnInit(): void {
    this.clasesService.detalle(this.id).subscribe({
      next: (clase) => {
        this.clase.set(clase);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar la clase'));
        this.cargando.set(false);
      },
    });
  }

  cambiarA(pestana: Pestana): void {
    this.pestana.set(pestana);
  }

  /** Nombres del profesorado, para la línea bajo el título. */
  nombresProfesorado(detalle: DetalleClase): string {
    return detalle.profesores.map(profesor => profesor.name).join(', ');
  }
}
