import { Component, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { UsuariosService } from '../../../core/services/usuarios.service';
import { EstadoCuenta, Invitacion, Pagina, Rol, Usuario } from '../../../core/models';
import { AvatarComponent } from '../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../../shared/pastilla-estado/pastilla-estado.component';
import { FechaPipe } from '../../../shared/pipes/fecha.pipe';

/**
 * Panel de usuarios.
 *
 * Las altas no llevan contraseña: se crea la cuenta y el sistema emite una
 * invitación de un solo uso. El enlace se muestra aquí además de enviarse por
 * correo, porque en un despliegue de demostración lo normal es que no haya
 * servidor de correo configurado y sin el enlace a la vista el flujo quedaría
 * cortado.
 *
 * No hay botón de borrar: solo desactivar. Un centro no puede perder el
 * histórico académico de alguien porque se pulse mal.
 */
@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    NgIf, NgFor, ReactiveFormsModule,
    AvatarComponent, CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent,
    DialogoComponent, AvisoComponent, FechaPipe,
  ],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css',
})
export class UsuariosComponent implements OnInit {

  private readonly usuariosService = inject(UsuariosService);
  private readonly fb = inject(FormBuilder);

  readonly pagina = signal<Pagina<Usuario> | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly trabajandoCon = signal<string | null>(null);

  readonly filtros = this.fb.nonNullable.group({
    q: [''],
    role: [''],
    status: [''],
  });

  // --- alta ---
  readonly dialogoAbierto = signal(false);
  readonly creando = signal(false);
  readonly errorFormulario = signal<string | null>(null);
  readonly invitacionEmitida = signal<{ nombre: string; invitacion: Invitacion } | null>(null);
  readonly enlaceCopiado = signal(false);

  readonly formulario = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    role: ['student' as Rol, [Validators.required]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(pagina = 0): void {
    this.cargando.set(true);
    this.error.set(null);

    const { q, role, status } = this.filtros.getRawValue();

    this.usuariosService.listar({
      q,
      role: role as Rol | '',
      status: status as EstadoCuenta | '',
      page: pagina,
    }).subscribe({
      next: (resultado) => {
        this.pagina.set(resultado);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar los usuarios'));
        this.cargando.set(false);
      },
    });
  }

  limpiarFiltros(): void {
    this.filtros.reset({ q: '', role: '', status: '' });
    this.cargar();
  }

  // ------------------------------------------------------------------ alta

  abrirDialogo(): void {
    this.formulario.reset({ name: '', email: '', role: 'student' });
    this.errorFormulario.set(null);
    this.invitacionEmitida.set(null);
    this.enlaceCopiado.set(false);
    this.dialogoAbierto.set(true);
  }

  crear(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.creando()) {
      return;
    }

    this.creando.set(true);
    this.errorFormulario.set(null);

    const datos = this.formulario.getRawValue();

    this.usuariosService.crear(datos).subscribe({
      next: (resultado) => {
        this.creando.set(false);
        // El diálogo no se cierra: ahora muestra el enlace de invitación, que
        // es lo único que el administrador necesita llevarse de aquí
        this.invitacionEmitida.set({ nombre: resultado.user.name, invitacion: resultado.invitation });
        this.cargar();
      },
      error: (err) => {
        this.creando.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  copiarEnlace(enlace: string): void {
    navigator.clipboard?.writeText(enlace).then(
      () => {
        this.enlaceCopiado.set(true);
        setTimeout(() => this.enlaceCopiado.set(false), 2200);
      },
      () => { /* sin portapapeles el enlace sigue visible para copiarlo a mano */ }
    );
  }

  // --------------------------------------------------------------- acciones

  cambiarEstado(usuario: Usuario, status: 'active' | 'disabled'): void {
    this.trabajandoCon.set(usuario.id);
    this.error.set(null);

    this.usuariosService.cambiarEstado(usuario.id, status).subscribe({
      next: (actualizado) => {
        this.trabajandoCon.set(null);
        this.pagina.update(p => p && ({
          ...p,
          contenido: p.contenido.map(u => u.id === actualizado.id ? actualizado : u),
        }));
      },
      error: (err) => {
        this.trabajandoCon.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  reenviarInvitacion(usuario: Usuario): void {
    this.trabajandoCon.set(usuario.id);
    this.error.set(null);

    this.usuariosService.reenviarInvitacion(usuario.id).subscribe({
      next: (invitacion) => {
        this.trabajandoCon.set(null);
        this.invitacionEmitida.set({ nombre: usuario.name, invitacion });
        this.dialogoAbierto.set(true);
      },
      error: (err) => {
        this.trabajandoCon.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
