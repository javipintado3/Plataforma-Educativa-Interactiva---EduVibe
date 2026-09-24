import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';

import { ClasesService } from '../../../../core/services/clases.service';
import { ConfirmacionService } from '../../../../core/services/confirmacion.service';
import { Miembro } from '../../../../core/models';
import { AuthService } from '../../../../core/services/auth.service';
import { AvatarComponent } from '../../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';

/**
 * Pestaña "Personas": quién imparte la clase y quién la cursa.
 *
 * Solo la administración puede dar de baja a alguien, y la propia API impide
 * quitar al último profesor. Aquí el botón se oculta a quien no puede, para no
 * ofrecer una acción que va a terminar en un error.
 */
@Component({
  selector: 'app-pestana-personas',
  standalone: true,
  imports: [NgIf, NgFor, AvatarComponent, CargandoComponent, EstadoVacioComponent, AvisoComponent],
  templateUrl: './pestana-personas.component.html',
  styleUrl: './pestana-personas.component.css',
})
export class PestanaPersonasComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  readonly auth = inject(AuthService);

  @Input({ required: true }) claseId!: string;

  readonly miembros = signal<Miembro[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly quitando = signal<string | null>(null);

  readonly profesorado = computed(() => this.miembros().filter(m => m.roleInClass === 'teacher'));
  readonly alumnado = computed(() => this.miembros().filter(m => m.roleInClass === 'student'));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clasesService.miembros(this.claseId).subscribe({
      next: (miembros) => {
        this.miembros.set(miembros);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar las personas'));
        this.cargando.set(false);
      },
    });
  }

  async quitar(miembro: Miembro): Promise<void> {
    const confirmado = await this.confirmacion.preguntar(`¿Quitar a ${miembro.name} de esta clase?`, {
      titulo: 'Quitar de la clase', textoConfirmar: 'Quitar',
    });
    if (!confirmado) {
      return;
    }

    this.quitando.set(miembro.userId);
    this.error.set(null);

    this.clasesService.desmatricular(this.claseId, miembro.userId).subscribe({
      next: () => {
        this.quitando.set(null);
        this.miembros.update(lista => lista.filter(m => m.userId !== miembro.userId));
      },
      error: (err) => {
        this.quitando.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
