import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { ClasesService } from '../../../core/services/clases.service';
import { SubidasService } from '../../../core/services/subidas.service';
import { DetalleClase } from '../../../core/models';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../shared/dialogo/dialogo.component';
import { PALETA_CLASE } from '../../../shared/paleta-clase';
import { SubidaArchivoComponent } from '../../../shared/subida-archivo/subida-archivo.component';
import { PestanaAvisosComponent } from './pestanas/pestana-avisos.component';
import { PestanaCalificacionesComponent } from './pestanas/pestana-calificaciones.component';
import { PestanaMaterialesComponent } from './pestanas/pestana-materiales.component';
import { PestanaPersonasComponent } from './pestanas/pestana-personas.component';
import { PestanaTrabajoComponent } from './pestanas/pestana-trabajo.component';
import { PortadaClaseComponent } from '../../../shared/portada-clase/portada-clase.component';

type Pestana = 'avisos' | 'trabajo' | 'materiales' | 'personas' | 'calificaciones';

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
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    CargandoComponent, AvisoComponent, DialogoComponent, PortadaClaseComponent, SubidaArchivoComponent,
    PestanaAvisosComponent, PestanaTrabajoComponent, PestanaMaterialesComponent,
    PestanaPersonasComponent, PestanaCalificacionesComponent,
  ],
  templateUrl: './detalle-clase.component.html',
  styleUrl: './detalle-clase.component.css',
})
export class DetalleClaseComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly subidasService = inject(SubidasService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  /** Llega de la ruta /clases/:id gracias a withComponentInputBinding(). */
  @Input() id = '';

  readonly clase = signal<DetalleClase | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly pestana = signal<Pestana>('trabajo');

  readonly paleta = PALETA_CLASE;
  readonly dialogoEditarAbierto = signal(false);
  readonly guardandoClase = signal(false);
  readonly errorEdicion = signal<string | null>(null);
  readonly subiendoPortada = signal(false);

  readonly formularioClase = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    subject: [''],
    color: [PALETA_CLASE[0].valor],
    imageUrl: [''],
  });

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

  abrirEditar(): void {
    const detalle = this.clase();
    if (!detalle) {
      return;
    }
    this.formularioClase.reset({
      name: detalle.name,
      subject: detalle.subject ?? '',
      color: detalle.color ?? PALETA_CLASE[0].valor,
      imageUrl: detalle.imageUrl ?? '',
    });
    this.errorEdicion.set(null);
    this.dialogoEditarAbierto.set(true);
  }

  /**
   * Acceso rápido: cambia la portada sin pasar por el diálogo de edición
   * completo. Sube el archivo y, en cuanto hay URL, guarda la clase con ese
   * único cambio.
   */
  cambiarPortadaDirecta(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0];
    const detalle = this.clase();
    if (!archivo || !detalle || this.subiendoPortada()) {
      return;
    }

    this.subiendoPortada.set(true);
    this.error.set(null);

    this.subidasService.subir(archivo, 'imagen').pipe(
      switchMap(url => this.clasesService.actualizar(this.id, {
        name: detalle.name,
        subject: detalle.subject || undefined,
        color: detalle.color || PALETA_CLASE[0].valor,
        imageUrl: url,
      })),
    ).subscribe({
      next: (actualizada) => {
        this.subiendoPortada.set(false);
        this.clase.set(actualizada);
      },
      error: (err) => {
        this.subiendoPortada.set(false);
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cambiar la portada'));
      },
    });
    input.value = '';
  }

  guardarClase(): void {
    this.formularioClase.markAllAsTouched();

    if (this.formularioClase.invalid || this.guardandoClase()) {
      return;
    }

    this.guardandoClase.set(true);
    this.errorEdicion.set(null);

    const { name, subject, color, imageUrl } = this.formularioClase.getRawValue();

    this.clasesService.actualizar(this.id, {
      name, subject: subject || undefined, color, imageUrl: imageUrl || undefined,
    }).subscribe({
      next: (actualizada) => {
        this.guardandoClase.set(false);
        this.dialogoEditarAbierto.set(false);
        this.clase.set(actualizada);
      },
      error: (err) => {
        this.guardandoClase.set(false);
        this.errorEdicion.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
