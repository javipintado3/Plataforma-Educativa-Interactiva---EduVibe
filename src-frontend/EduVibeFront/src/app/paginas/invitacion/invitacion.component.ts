import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { DatosInvitacion } from '../../core/models';
import { AvisoComponent } from '../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../shared/cargando/cargando.component';
import { LogoComponent } from '../../shared/logo/logo.component';
import { FechaPipe } from '../../shared/pipes/fecha.pipe';

/**
 * Activación de una cuenta con el enlace de invitación.
 *
 * Primero se comprueba el enlace y se muestra a quién corresponde: así la
 * persona ve que ha llegado al sitio correcto antes de escribir una
 * contraseña. Si el enlace no vale, se dice y se acabó, sin pedir nada.
 *
 * Al terminar no se manda al login: la API ya devuelve la sesión iniciada, y
 * hacer teclear dos veces la misma contraseña recién creada no tiene sentido.
 */
@Component({
  selector: 'app-invitacion',
  standalone: true,
  imports: [NgIf, ReactiveFormsModule, AvisoComponent, CargandoComponent, LogoComponent, FechaPipe],
  templateUrl: './invitacion.component.html',
  styleUrl: './invitacion.component.css',
})
export class InvitacionComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Llega de la ruta gracias a withComponentInputBinding(). */
  @Input() token = '';

  readonly formulario = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    repetir: ['', [Validators.required]],
  });

  readonly comprobando = signal(true);
  readonly enviando = signal(false);
  readonly datos = signal<DatosInvitacion | null>(null);
  readonly errorEnlace = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.auth.consultarInvitacion(this.token).subscribe({
      next: (datos) => {
        this.datos.set(datos);
        this.comprobando.set(false);
      },
      error: (err) => {
        this.errorEnlace.set(AvisoComponent.mensajeDe(
          err, 'La invitación no es válida o ha caducado.'));
        this.comprobando.set(false);
      },
    });
  }

  get noCoinciden(): boolean {
    const { password, repetir } = this.formulario.getRawValue();
    return !!repetir && this.formulario.controls.repetir.touched && password !== repetir;
  }

  campoInvalido(nombre: 'password' | 'repetir'): boolean {
    const campo = this.formulario.controls[nombre];
    return campo.invalid && campo.touched;
  }

  activar(): void {
    this.formulario.markAllAsTouched();

    if (this.formulario.invalid || this.noCoinciden || this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    this.auth.aceptarInvitacion(this.token, this.formulario.getRawValue().password).subscribe({
      next: () => this.router.navigateByUrl('/clases'),
      error: (err) => {
        this.enviando.set(false);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
