import { Component, Input, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ClasesService } from '../../../../core/services/clases.service';
import { ConfirmacionService } from '../../../../core/services/confirmacion.service';
import { SubgruposService } from '../../../../core/services/subgrupos.service';
import { Miembro, Subgrupo } from '../../../../core/models';
import { AuthService } from '../../../../core/services/auth.service';
import { AvatarComponent } from '../../../../shared/avatar/avatar.component';
import { AvisoComponent } from '../../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../../../shared/estado-vacio/estado-vacio.component';

/**
 * Pestaña "Personas": quién imparte la clase, quién la cursa, y sus subgrupos.
 *
 * Solo la administración puede dar de baja a alguien, y la propia API impide
 * quitar al último profesor. Aquí el botón se oculta a quien no puede, para no
 * ofrecer una acción que va a terminar en un error.
 *
 * Los subgrupos los gestiona el profesorado de la clase: son la base de las
 * tareas grupales, así que viven aquí, junto al resto de quién es quién.
 */
@Component({
  selector: 'app-pestana-personas',
  standalone: true,
  imports: [
    NgIf, NgFor, ReactiveFormsModule,
    AvatarComponent, CargandoComponent, EstadoVacioComponent, AvisoComponent, DialogoComponent,
  ],
  templateUrl: './pestana-personas.component.html',
  styleUrl: './pestana-personas.component.css',
})
export class PestanaPersonasComponent implements OnInit {

  private readonly clasesService = inject(ClasesService);
  private readonly subgruposService = inject(SubgruposService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  @Input({ required: true }) claseId!: string;
  @Input() puedoEditar = false;

  readonly miembros = signal<Miembro[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly quitando = signal<string | null>(null);

  readonly profesorado = computed(() => this.miembros().filter(m => m.roleInClass === 'teacher'));
  readonly alumnado = computed(() => this.miembros().filter(m => m.roleInClass === 'student'));

  // --- subgrupos ---
  readonly subgrupos = signal<Subgrupo[]>([]);
  readonly cargandoSubgrupos = signal(true);

  readonly dialogoSubgrupoAbierto = signal(false);
  readonly editando = signal<Subgrupo | null>(null);
  readonly guardandoSubgrupo = signal(false);
  readonly borrandoSubgrupo = signal<string | null>(null);
  readonly errorSubgrupo = signal<string | null>(null);

  readonly formSubgrupo = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    memberIds: this.fb.nonNullable.control<string[]>([]),
  });

  ngOnInit(): void {
    this.cargar();
    this.cargarSubgrupos();
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

  // ------------------------------------------------------------ subgrupos

  cargarSubgrupos(): void {
    this.cargandoSubgrupos.set(true);

    this.clasesService.subgrupos(this.claseId).subscribe({
      next: (subgrupos) => {
        this.subgrupos.set(subgrupos);
        this.cargandoSubgrupos.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se han podido cargar los subgrupos'));
        this.cargandoSubgrupos.set(false);
      },
    });
  }

  abrirCrearSubgrupo(): void {
    this.editando.set(null);
    this.formSubgrupo.reset({ name: '', memberIds: [] });
    this.errorSubgrupo.set(null);
    this.dialogoSubgrupoAbierto.set(true);
  }

  abrirEditarSubgrupo(grupo: Subgrupo): void {
    this.editando.set(grupo);
    this.formSubgrupo.reset({ name: grupo.name, memberIds: grupo.members.map(m => m.id) });
    this.errorSubgrupo.set(null);
    this.dialogoSubgrupoAbierto.set(true);
  }

  nombresDe(miembros: { name: string }[]): string {
    return miembros.length ? miembros.map(m => m.name).join(', ') : 'Sin miembros';
  }

  perteneceAlFormulario(userId: string): boolean {
    return this.formSubgrupo.controls.memberIds.value.includes(userId);
  }

  alternarMiembro(userId: string, marcado: boolean): void {
    const actuales = this.formSubgrupo.controls.memberIds.value;
    this.formSubgrupo.controls.memberIds.setValue(
      marcado ? [...actuales, userId] : actuales.filter(id => id !== userId));
  }

  guardarSubgrupo(): void {
    this.formSubgrupo.markAllAsTouched();

    if (this.formSubgrupo.invalid || !this.formSubgrupo.value.memberIds?.length || this.guardandoSubgrupo()) {
      if (!this.formSubgrupo.value.memberIds?.length) {
        this.errorSubgrupo.set('Elige al menos un miembro.');
      }
      return;
    }

    this.guardandoSubgrupo.set(true);
    this.errorSubgrupo.set(null);

    const datos = this.formSubgrupo.getRawValue();
    const edicion = this.editando();

    const peticion = edicion
      ? this.subgruposService.actualizar(edicion.id, datos)
      : this.clasesService.crearSubgrupo(this.claseId, datos);

    peticion.subscribe({
      next: () => {
        this.guardandoSubgrupo.set(false);
        this.dialogoSubgrupoAbierto.set(false);
        this.cargarSubgrupos();
      },
      error: (err) => {
        this.guardandoSubgrupo.set(false);
        this.errorSubgrupo.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async borrarSubgrupo(grupo: Subgrupo): Promise<void> {
    const confirmado = await this.confirmacion.preguntar(`¿Borrar el subgrupo "${grupo.name}"?`, {
      titulo: 'Borrar subgrupo', textoConfirmar: 'Borrar',
    });
    if (!confirmado) {
      return;
    }

    this.borrandoSubgrupo.set(grupo.id);
    this.error.set(null);

    this.subgruposService.eliminar(grupo.id).subscribe({
      next: () => {
        this.borrandoSubgrupo.set(null);
        this.subgrupos.update(lista => lista.filter(g => g.id !== grupo.id));
      },
      error: (err) => {
        this.borrandoSubgrupo.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }
}
