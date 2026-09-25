import { Component, Input, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { ConfirmacionService } from '../../../core/services/confirmacion.service';
import { ExamenesService } from '../../../core/services/examenes.service';
import { IntentoExamen } from '../../../core/models';
import { AvisoComponent } from '../../../shared/aviso/aviso.component';
import { CargandoComponent } from '../../../shared/cargando/cargando.component';

/**
 * Hacer el examen: el temporizador corre desde que se carga esta pantalla
 * hasta que se entrega o se acaba el tiempo, momento en el que se entrega
 * sola. Cada respuesta se autoguarda al marcarla, así que recargar la página
 * no pierde nada: {@link ExamenesService.comenzarOReanudar} devuelve el mismo
 * intento con lo ya respondido.
 */
@Component({
  selector: 'app-hacer-examen',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, CargandoComponent, AvisoComponent],
  templateUrl: './hacer-examen.component.html',
  styleUrl: './hacer-examen.component.css',
})
export class HacerExamenComponent implements OnInit, OnDestroy {

  private readonly examenesService = inject(ExamenesService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly router = inject(Router);

  @Input() id = '';

  readonly intento = signal<IntentoExamen | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly entregando = signal(false);
  readonly segundosRestantes = signal(0);

  private intervalo?: ReturnType<typeof setInterval>;
  private entregadoYa = false;

  readonly tiempoFormateado = computed(() => {
    const total = this.segundosRestantes();
    const h = Math.floor(total / 3600);
    const m = Math.floor((total % 3600) / 60);
    const s = total % 60;
    const dos = (n: number) => String(n).padStart(2, '0');
    return h > 0 ? `${h}:${dos(m)}:${dos(s)}` : `${m}:${dos(s)}`;
  });

  readonly pocoTiempo = computed(() => this.segundosRestantes() > 0 && this.segundosRestantes() <= 60);

  ngOnInit(): void {
    this.examenesService.comenzarOReanudar(this.id).subscribe({
      next: (intento) => {
        if (intento.entregado) {
          // Se llega aquí a mano tras haber entregado ya: no hay nada que hacer
          this.router.navigate(['/examenes', this.id]);
          return;
        }
        this.intento.set(intento);
        this.cargando.set(false);
        this.iniciarCronometro(intento.deadline);
      },
      error: (err) => {
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido abrir el examen'));
        this.cargando.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalo);
  }

  private iniciarCronometro(deadline: string): void {
    const limite = new Date(deadline).getTime();

    const actualizar = () => {
      const restante = Math.max(0, Math.round((limite - Date.now()) / 1000));
      this.segundosRestantes.set(restante);

      if (restante <= 0) {
        clearInterval(this.intervalo);
        this.entregar(true);
      }
    };

    actualizar();
    this.intervalo = setInterval(actualizar, 1000);
  }

  elegir(questionId: string, optionId: string): void {
    const intento = this.intento();
    if (!intento || this.entregando()) {
      return;
    }

    // Optimista: se pinta al momento y se guarda en segundo plano
    this.intento.set({
      ...intento,
      questions: intento.questions.map(p => p.id === questionId ? { ...p, miRespuesta: optionId } : p),
    });

    this.examenesService.guardarRespuesta(this.id, questionId, optionId).subscribe({
      error: (err) => this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido guardar la respuesta')),
    });
  }

  /** Cuántas preguntas quedan sin responder, para el aviso antes de entregar. */
  get sinResponder(): number {
    return this.intento()?.questions.filter(p => !p.miRespuesta).length ?? 0;
  }

  async entregarConConfirmacion(): Promise<void> {
    const mensaje = this.sinResponder
      ? `Te quedan ${this.sinResponder} pregunta(s) sin responder. ¿Entregar de todas formas?`
      : '¿Entregar el examen? No podrás cambiar tus respuestas.';

    const confirmado = await this.confirmacion.preguntar(mensaje, {
      titulo: 'Entregar examen', textoConfirmar: 'Entregar',
    });
    if (confirmado) {
      this.entregar(false);
    }
  }

  private entregar(porTiempo: boolean): void {
    if (this.entregadoYa) {
      return;
    }
    this.entregadoYa = true;
    this.entregando.set(true);
    this.error.set(null);

    this.examenesService.entregar(this.id).subscribe({
      next: () => this.router.navigate(['/examenes', this.id]),
      error: (err) => {
        this.entregando.set(false);
        this.entregadoYa = false;
        // Si el tiempo se acabó también en el servidor, no tiene sentido reintentar
        this.error.set(AvisoComponent.mensajeDe(err,
          porTiempo ? 'Se acabó el tiempo y no se ha podido entregar' : 'No se ha podido entregar'));
      },
    });
  }
}
