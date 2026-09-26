import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ForoService } from '../../../core/services/foro.service';
import { ConfirmacionService } from '../../../core/services/confirmacion.service';
import { DetalleHiloForo, MensajeForo } from '../../../core/models';
import { AvatarComponent } from '../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';
import { FechaPipe } from '../../../shared/pipes/fecha.pipe';

/**
 * Un hilo del foro: sus mensajes en orden, con una caja para responder al
 * final. El primer mensaje no es un campo aparte del hilo: es un mensaje más
 * de la lista, así que no hay una plantilla especial para él.
 */
@Component({
  selector: 'app-detalle-hilo',
  standalone: true,
  imports: [
    NgIf, NgFor, RouterLink, ReactiveFormsModule,
    CargandoComponent, AvisoComponent, AvatarComponent, FechaPipe,
  ],
  templateUrl: './detalle-hilo.component.html',
  styleUrl: './detalle-hilo.component.css',
})
export class DetalleHiloComponent implements OnInit {

  private readonly foroService = inject(ForoService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  @Input() id = '';

  readonly hilo = signal<DetalleHiloForo | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly borrandoHilo = signal(false);
  readonly borrandoMensaje = signal<string | null>(null);

  readonly respondiendo = signal(false);

  readonly formRespuesta = this.fb.nonNullable.group({
    content: ['', [Validators.required]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);

    this.foroService.detalle(this.id).subscribe({
      next: (hilo) => {
        this.hilo.set(hilo);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar el hilo'));
        this.cargando.set(false);
      },
    });
  }

  responder(): void {
    this.formRespuesta.markAllAsTouched();

    if (this.formRespuesta.invalid || this.respondiendo()) {
      return;
    }

    this.respondiendo.set(true);
    this.error.set(null);

    const { content } = this.formRespuesta.getRawValue();

    this.foroService.responder(this.id, content).subscribe({
      next: (mensaje) => {
        this.respondiendo.set(false);
        this.formRespuesta.reset({ content: '' });
        this.hilo.update(h => h ? { ...h, posts: [...h.posts, mensaje] } : h);
      },
      error: (err) => {
        this.respondiendo.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async borrarMensaje(mensaje: MensajeForo): Promise<void> {
    const confirmado = await this.confirmacion.preguntar('¿Retirar este mensaje del hilo?', {
      titulo: 'Retirar mensaje', textoConfirmar: 'Retirar',
    });
    if (!confirmado) {
      return;
    }

    this.borrandoMensaje.set(mensaje.id);
    this.error.set(null);

    this.foroService.borrarMensaje(mensaje.id).subscribe({
      next: () => {
        this.borrandoMensaje.set(null);
        this.hilo.update(h => h ? { ...h, posts: h.posts.filter(p => p.id !== mensaje.id) } : h);
      },
      error: (err) => {
        this.borrandoMensaje.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async borrarHilo(): Promise<void> {
    const hilo = this.hilo();
    if (!hilo || this.borrandoHilo()) {
      return;
    }

    const confirmado = await this.confirmacion.preguntar('¿Borrar este hilo entero, con todos sus mensajes?', {
      titulo: 'Borrar hilo', textoConfirmar: 'Borrar',
    });
    if (!confirmado) {
      return;
    }

    this.borrandoHilo.set(true);
    this.error.set(null);

    this.foroService.borrarHilo(hilo.id).subscribe({
      next: () => this.router.navigate(['/clases', hilo.classId]),
      error: (err) => {
        this.borrandoHilo.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
