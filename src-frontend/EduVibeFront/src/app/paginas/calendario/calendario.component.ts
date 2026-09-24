import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from '../../core/services/auth.service';
import { CalendarioService } from '../../core/services/calendario.service';
import { ClasesService } from '../../core/services/clases.service';
import { ConfirmacionService } from '../../core/services/confirmacion.service';
import { Clase, EntradaAgenda, TipoEventoAgenda } from '../../core/models';
import { AvisoComponent } from '../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../shared/cargando/cargando.component';
import { DialogoComponent } from '../../shared/dialogo/dialogo.component';
import { EstadoVacioComponent } from '../../shared/estado-vacio/estado-vacio.component';
import { PastillaEstadoComponent } from '../../shared/pastilla-estado/pastilla-estado.component';
import { FechaPipe } from '../../shared/pipes/fecha.pipe';

const MESES = [
  'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
  'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre',
];

/**
 * Agenda del centro: eventos a mano y fechas de entrega, mes a mes.
 *
 * La API ya las devuelve mezcladas y ordenadas (ver CalendarService en el
 * backend), así que aquí solo hay que pintarlas y, para quien puede
 * gestionarlas, dejar crear y borrar eventos.
 */
@Component({
  selector: 'app-calendario',
  standalone: true,
  imports: [
    NgIf, NgFor, ReactiveFormsModule,
    CargandoComponent, EstadoVacioComponent, DialogoComponent, AvisoComponent,
    PastillaEstadoComponent, FechaPipe,
  ],
  templateUrl: './calendario.component.html',
  styleUrl: './calendario.component.css',
})
export class CalendarioComponent implements OnInit {

  private readonly calendarioService = inject(CalendarioService);
  private readonly clasesService = inject(ClasesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  readonly mesActual = signal(this.inicioDeMes(new Date()));
  readonly nombreMes = computed(() => {
    const mes = this.mesActual();
    return `${MESES[mes.getMonth()]} ${mes.getFullYear()}`;
  });

  readonly entradas = signal<EntradaAgenda[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly borrando = signal<string | null>(null);

  /** Las clases que gestiono: para el selector del formulario y para saber qué eventos puedo borrar. */
  readonly misClases = signal<Clase[]>([]);
  private readonly idsDeMisClases = computed(() => new Set(this.misClases().map(c => c.id)));

  readonly puedeCrear = computed(() => this.auth.esAdmin() || this.auth.esProfesor());

  readonly dialogoAbierto = signal(false);
  readonly creando = signal(false);
  readonly errorFormulario = signal<string | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    eventDate: ['', [Validators.required]],
    type: this.fb.nonNullable.control<TipoEventoAgenda | ''>(''),
    // Sin Validators.required: vacío es válido para la administración (evento
    // de todo el centro); para el resto se comprueba a mano en crear().
    classId: [''],
  });

  ngOnInit(): void {
    if (this.puedeCrear()) {
      this.clasesService.misClases().subscribe(clases => this.misClases.set(clases));
    }
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    const desde = this.mesActual();
    const hasta = new Date(desde.getFullYear(), desde.getMonth() + 1, 1);

    this.calendarioService.agenda(desde, hasta).subscribe({
      next: (entradas) => {
        this.entradas.set(entradas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido cargar la agenda'));
        this.cargando.set(false);
      },
    });
  }

  mesAnterior(): void {
    const mes = this.mesActual();
    this.mesActual.set(new Date(mes.getFullYear(), mes.getMonth() - 1, 1));
    this.cargar();
  }

  mesSiguiente(): void {
    const mes = this.mesActual();
    this.mesActual.set(new Date(mes.getFullYear(), mes.getMonth() + 1, 1));
    this.cargar();
  }

  /** Solo se puede borrar un evento a mano de una clase que se gestiona; las entregas no son eventos reales. */
  puedeBorrar(entrada: EntradaAgenda): boolean {
    if (entrada.origen !== 'event') {
      return false;
    }
    if (this.auth.esAdmin()) {
      return true;
    }
    return entrada.classId !== null && this.idsDeMisClases().has(entrada.classId);
  }

  abrirDialogo(): void {
    this.formulario.reset({
      title: '',
      eventDate: '',
      type: '',
      classId: this.auth.esAdmin() ? '' : (this.misClases()[0]?.id ?? ''),
    });
    this.errorFormulario.set(null);
    this.dialogoAbierto.set(true);
  }

  crear(): void {
    this.formulario.markAllAsTouched();

    // Solo la administración puede dejar la clase sin elegir (evento de todo el centro)
    if (!this.auth.esAdmin() && !this.formulario.controls.classId.value) {
      return;
    }
    if (this.formulario.invalid || this.creando()) {
      return;
    }

    this.creando.set(true);
    this.errorFormulario.set(null);

    const { title, eventDate, type, classId } = this.formulario.getRawValue();

    this.calendarioService.crear({
      title,
      // El input datetime-local da "2026-10-02T18:30"; la API espera un
      // instante en UTC, y eso es justo lo que hace toISOString()
      eventDate: new Date(eventDate).toISOString(),
      // El @Pattern del backend admite null pero no "": sin esto, dejar el
      // tipo sin especificar devolvía 400 en vez de crear el evento.
      type: type || undefined,
      classId: classId || null,
    }).subscribe({
      next: () => {
        this.creando.set(false);
        this.dialogoAbierto.set(false);
        this.cargar();
      },
      error: (err) => {
        this.creando.set(false);
        this.errorFormulario.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  async borrar(entrada: EntradaAgenda): Promise<void> {
    const confirmado = await this.confirmacion.preguntar(`¿Borrar "${entrada.title}" de la agenda?`, {
      titulo: 'Borrar evento', textoConfirmar: 'Borrar',
    });
    if (!confirmado) {
      return;
    }

    this.borrando.set(entrada.referencia);
    this.error.set(null);

    this.calendarioService.eliminar(entrada.referencia).subscribe({
      next: () => {
        this.borrando.set(null);
        this.entradas.update(lista => lista.filter(e => e.referencia !== entrada.referencia));
      },
      error: (err) => {
        this.borrando.set(null);
        this.error.set(AvisoComponent.mensajeDe(err));
      },
    });
  }

  horaDe(fechaIso: string): string {
    const fecha = new Date(fechaIso);
    const hora = String(fecha.getHours()).padStart(2, '0');
    const minuto = String(fecha.getMinutes()).padStart(2, '0');
    return `${hora}:${minuto}`;
  }

  private inicioDeMes(fecha: Date): Date {
    return new Date(fecha.getFullYear(), fecha.getMonth(), 1);
  }
}
