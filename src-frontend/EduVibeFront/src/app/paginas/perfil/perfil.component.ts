import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { ClasesService } from '../../core/services/clases.service';
import { NotificacionesService } from '../../core/services/notificaciones.service';
import { PerfilService } from '../../core/services/perfil.service';
import { Clase, Notificacion, ResumenPerfil } from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../shared/pastilla-estado/pastilla-estado.component';
import { SubidaArchivoComponent } from '../../shared/subida-archivo/subida-archivo.component';
import { TarjetaClaseComponent } from '../../shared/tarjeta-clase/tarjeta-clase.component';
import { FechaPipe } from '../../shared/pipes/fecha.pipe';

/** A qué ruta lleva cada tipo de notificación, según lo que traiga en su payload. */
function rutaDe(notificacion: Notificacion): string | null {
  const payload = notificacion.payload ?? {};

  if (notificacion.type === 'announcement_created' && payload['classId']) {
    return `/clases/${payload['classId']}`;
  }
  if ((notificacion.type === 'assignment_created' || notificacion.type === 'grade_published') && payload['assignmentId']) {
    return `/tareas/${payload['assignmentId']}`;
  }
  return null;
}

/** Cómo se lee cada rol en el desglose de administración. */
const ETIQUETAS_ROL: Record<string, string> = {
  admin: 'Administración', teacher: 'Profesorado', student: 'Alumnado', guardian: 'Tutores legales',
};

/**
 * Perfil: quién eres, y lo que te interesa ver según tu rol.
 *
 * No es la misma pantalla para todos —eso es justo el punto—: al alumnado le
 * importa lo que tiene pendiente, al profesorado cuánto le queda por
 * corregir, y a administración el estado del centro. `ResumenPerfil` llega
 * ya calculado del backend; aquí solo se decide qué tarjetas enseñar.
 */
@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [
    NgIf, NgFor, FormsModule, RouterLink, DecimalPipe,
    AvatarComponent, AvisoComponent, CargandoComponent, EstadoVacioComponent,
    PastillaEstadoComponent, SubidaArchivoComponent, TarjetaClaseComponent, FechaPipe,
  ],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css',
})
export class PerfilComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly perfilService = inject(PerfilService);
  private readonly notificacionesService = inject(NotificacionesService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  readonly resumen = signal<ResumenPerfil | null>(null);
  readonly clases = signal<Clase[]>([]);
  readonly notificaciones = signal<Notificacion[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly marcandoTodas = signal(false);
  readonly guardandoAvatar = signal(false);

  readonly usuariosPorRol = computed(() => {
    const usuarios = this.resumen()?.usuariosPorRol;
    if (!usuarios) {
      return [];
    }
    const orden = ['admin', 'teacher', 'student', 'guardian'];
    return Object.entries(usuarios).sort((a, b) => orden.indexOf(a[0]) - orden.indexOf(b[0]));
  });

  readonly hayNoLeidas = computed(() => this.notificaciones().some(n => !n.leida));

  ngOnInit(): void {
    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      resumen: this.perfilService.resumen(),
      clases: this.clasesService.misClases(),
      notificaciones: this.notificacionesService.misNotificaciones(),
    }).subscribe({
      next: ({ resumen, clases, notificaciones }) => {
        this.resumen.set(resumen);
        this.clases.set(clases);
        this.notificaciones.set(notificaciones);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar el perfil'));
        this.cargando.set(false);
      },
    });
  }

  etiquetaRol(rol: string): string {
    return ETIQUETAS_ROL[rol] ?? rol;
  }

  /** Se sube al elegir el archivo (lo hace app-subida-archivo); aquí solo queda guardarla en el perfil. */
  cambiarAvatar(url: string): void {
    if (this.guardandoAvatar()) {
      return;
    }
    this.guardandoAvatar.set(true);

    this.perfilService.actualizarAvatar(url).subscribe({
      next: () => {
        this.guardandoAvatar.set(false);
        this.auth.refrescarUsuario().subscribe();
      },
      error: (err) => {
        this.guardandoAvatar.set(false);
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido actualizar la foto'));
      },
    });
  }

  marcarLeida(notificacion: Notificacion): void {
    if (notificacion.leida) {
      return;
    }
    this.notificacionesService.marcarLeida(notificacion.id).subscribe(() => {
      this.notificaciones.update(lista =>
        lista.map(n => n.id === notificacion.id ? { ...n, leida: true } : n));
    });
  }

  /** Marca leída y, si la notificación trae dónde ir, navega. */
  irA(notificacion: Notificacion): void {
    this.marcarLeida(notificacion);
    const ruta = rutaDe(notificacion);
    if (ruta) {
      this.router.navigateByUrl(ruta);
    }
  }

  marcarTodasLeidas(): void {
    if (this.marcandoTodas() || !this.hayNoLeidas()) {
      return;
    }
    this.marcandoTodas.set(true);

    this.notificacionesService.marcarTodasLeidas().subscribe({
      next: () => {
        this.marcandoTodas.set(false);
        this.notificaciones.update(lista => lista.map(n => ({ ...n, leida: true })));
      },
      error: () => this.marcandoTodas.set(false),
    });
  }
}
