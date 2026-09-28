import { Component, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { ConfirmacionService } from '../../../core/services/confirmacion.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { EstadoCuenta, Invitacion, Pagina, ResultadoImportacion, Rol, Usuario } from '../../../core/models';
import { AvatarComponent } from '../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../shared/estado-vacio/estado-vacio.component';
import { LimpiarFiltrosComponent } from '../../../shared/limpiar-filtros/limpiar-filtros.component';
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
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    AvatarComponent, CargandoComponent, EstadoVacioComponent, PastillaEstadoComponent, LimpiarFiltrosComponent,
    DialogoComponent, AvisoComponent, FechaPipe,
  ],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css',
})
export class UsuariosComponent implements OnInit {

  private readonly usuariosService = inject(UsuariosService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);

  readonly pagina = signal<Pagina<Usuario> | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly trabajandoCon = signal<string | null>(null);

  // --- selección en lote ---
  readonly seleccionados = signal<Set<string>>(new Set());
  readonly aplicandoLote = signal(false);

  estaSeleccionado(usuarioId: string): boolean {
    return this.seleccionados().has(usuarioId);
  }

  alternarSeleccion(usuario: Usuario, marcado: boolean): void {
    this.seleccionados.update(actuales => {
      const nuevo = new Set(actuales);
      marcado ? nuevo.add(usuario.id) : nuevo.delete(usuario.id);
      return nuevo;
    });
  }

  get todosSeleccionadosEnPagina(): boolean {
    const contenido = this.pagina()?.contenido ?? [];
    return contenido.length > 0 && contenido.every(u => this.estaSeleccionado(u.id));
  }

  alternarTodosEnPagina(marcado: boolean): void {
    const contenido = this.pagina()?.contenido ?? [];
    this.seleccionados.update(actuales => {
      const nuevo = new Set(actuales);
      for (const usuario of contenido) {
        marcado ? nuevo.add(usuario.id) : nuevo.delete(usuario.id);
      }
      return nuevo;
    });
  }

  limpiarSeleccion(): void {
    this.seleccionados.set(new Set());
  }

  /** Activa o desactiva todo lo seleccionado de golpe. Igual que activar una cuenta sola, pero repetido. */
  async cambiarEstadoLote(status: 'active' | 'disabled'): Promise<void> {
    const ids = Array.from(this.seleccionados());
    if (!ids.length || this.aplicandoLote()) {
      return;
    }

    const confirmado = await this.confirmacion.preguntar(
      `¿${status === 'active' ? 'Reactivar' : 'Desactivar'} ${ids.length} cuenta(s)?`,
      { titulo: status === 'active' ? 'Reactivar en lote' : 'Desactivar en lote', peligro: status === 'disabled' });
    if (!confirmado) {
      return;
    }

    this.aplicandoLote.set(true);
    this.error.set(null);

    forkJoin(ids.map(id => this.usuariosService.cambiarEstado(id, status))).subscribe({
      next: () => {
        this.aplicandoLote.set(false);
        this.limpiarSeleccion();
        this.cargar(this.pagina()?.pagina ?? 0);
      },
      error: (err) => {
        this.aplicandoLote.set(false);
        // Alguna cuenta del lote puede haber cambiado antes del fallo (por
        // ejemplo, la propia cuenta de quien administra no se puede
        // desactivar); se recarga para reflejar lo que sí se aplicó.
        this.error.set(AvisoComponent.mensajeDe(err));
        this.limpiarSeleccion();
        this.cargar(this.pagina()?.pagina ?? 0);
      },
    });
  }

  // --- CSV ---
  readonly exportando = signal(false);
  readonly dialogoImportarAbierto = signal(false);
  readonly ficheroImportar = signal<File | null>(null);
  readonly arrastrandoCsv = signal(false);
  readonly importando = signal(false);
  readonly errorImportar = signal<string | null>(null);
  readonly resultadoImportar = signal<ResultadoImportacion | null>(null);

  exportar(): void {
    if (this.exportando()) {
      return;
    }
    this.exportando.set(true);
    this.error.set(null);

    const { q, role, status } = this.filtros.getRawValue();

    this.usuariosService.exportarCsv({ q, role: role as Rol | '', status: status as EstadoCuenta | '' }).subscribe({
      next: (blob) => {
        this.exportando.set(false);
        this.descargar(blob, 'usuarios.csv');
      },
      error: (err) => {
        this.exportando.set(false);
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido exportar'));
      },
    });
  }

  private descargar(blob: Blob, nombre: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    // Algunos navegadores solo disparan la descarga si el <a> está en el
    // documento en el momento del click, no basta con crearlo al vuelo.
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    URL.revokeObjectURL(url);
  }

  abrirImportar(): void {
    this.ficheroImportar.set(null);
    this.arrastrandoCsv.set(false);
    this.errorImportar.set(null);
    this.resultadoImportar.set(null);
    this.dialogoImportarAbierto.set(true);
  }

  ficheroElegido(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.elegirFichero(input.files?.[0] ?? null);
    input.value = '';
  }

  onDragOverCsv(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrandoCsv.set(true);
  }

  onDragLeaveCsv(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrandoCsv.set(false);
  }

  onDropCsv(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrandoCsv.set(false);
    this.elegirFichero(evento.dataTransfer?.files?.[0] ?? null);
  }

  private elegirFichero(fichero: File | null): void {
    this.ficheroImportar.set(fichero);
    this.errorImportar.set(fichero && !fichero.name.toLowerCase().endsWith('.csv')
      ? 'Ese archivo no parece un CSV' : null);
  }

  quitarFichero(): void {
    this.ficheroImportar.set(null);
    this.errorImportar.set(null);
  }

  /** Tamaño legible del fichero elegido, para el chip del dropzone. */
  tamanoLegible(bytes: number): string {
    return bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`;
  }

  importarCsv(): void {
    const fichero = this.ficheroImportar();
    if (!fichero || this.errorImportar() || this.importando()) {
      return;
    }

    this.importando.set(true);
    this.errorImportar.set(null);

    this.usuariosService.importar(fichero).subscribe({
      next: (resultado) => {
        this.importando.set(false);
        this.resultadoImportar.set(resultado);
        this.cargar();
      },
      error: (err) => {
        this.importando.set(false);
        this.errorImportar.set(AvisoComponent.mensajeDe(err, 'No se ha podido importar el fichero'));
      },
    });
  }

  readonly filtros = this.fb.nonNullable.group({
    q: [''],
    role: [''],
    status: [''],
  });

  /** El botón de limpiar solo tiene sentido si hay algo que limpiar. */
  get hayFiltrosActivos(): boolean {
    const { q, role, status } = this.filtros.value;
    return !!(q || role || status);
  }

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
