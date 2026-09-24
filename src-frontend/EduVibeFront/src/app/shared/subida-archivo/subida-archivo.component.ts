import { Component, Input, forwardRef, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import { SubidasService, TipoSubida } from '../../core/services/subidas.service';
import { AvisoComponent } from '../aviso/aviso.component';
import { RutaArchivoPipe } from '../pipes/ruta-archivo.pipe';

/**
 * Campo de "imagen o archivo": una zona para arrastrar y soltar (o pinchar y
 * elegir), con un enlace aparte para quien prefiera pegar una URL.
 *
 * Es un ControlValueAccessor para poder usarlo con formControlName, igual
 * que un input normal: el formulario que lo usa no sabe ni le importa si el
 * valor final vino de una subida o de teclearlo.
 */
@Component({
  selector: 'app-subida-archivo',
  standalone: true,
  imports: [NgIf, RutaArchivoPipe],
  template: `
    <!-- Ya hay algo elegido: una ficha compacta en vez de la zona de subida -->
    <div class="archivo-elegido" *ngIf="valor() && !subiendo()">
      <img *ngIf="tipo === 'imagen'" class="miniatura" [src]="valor() | rutaArchivo" alt="">
      <svg *ngIf="tipo !== 'imagen'" class="icono-documento" width="20" height="20" viewBox="0 0 24 24"
           fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z"/><path d="M14 2v6h6"/>
      </svg>
      <span class="crecer nombre-archivo" [title]="valor()">{{ nombreDe(valor()) }}</span>
      <button type="button" class="boton boton-fantasma boton-pequeno" *ngIf="!bloqueado"
              [disabled]="deshabilitado()" (click)="quitar()">Quitar</button>
    </div>

    <!-- Nada elegido todavía, o subiendo -->
    <ng-container *ngIf="!valor() || subiendo()">

      <label class="zona-subida" *ngIf="!modoEnlace()" [class.arrastrando]="arrastrando()"
             [class.deshabilitada]="subiendo() || deshabilitado() || bloqueado"
             (dragover)="onDragOver($event)" (dragleave)="onDragLeave($event)" (drop)="onDrop($event)">
        <input type="file" class="entrada-oculta" [accept]="accept"
               [disabled]="subiendo() || deshabilitado() || bloqueado" (change)="onInputChange($event)">

        <ng-container *ngIf="!subiendo()">
          <span class="icono-mas" aria-hidden="true">+</span>
          <span class="texto-zona">Arrastra un archivo aquí o haz clic para subir</span>
          <span class="formatos-zona">{{ formatosAyuda }}</span>
        </ng-container>
        <span class="texto-zona" *ngIf="subiendo()">Subiendo…</span>
      </label>

      <button type="button" class="enlace-alternativo" *ngIf="!modoEnlace() && !subiendo() && !bloqueado"
              (click)="modoEnlace.set(true)">
        ¿Prefieres pegar un enlace?
      </button>

      <div class="campo-enlace" *ngIf="modoEnlace()">
        <input type="text" class="entrada crecer" [value]="valor()"
               (input)="escribir($any($event.target).value)" (blur)="onTouched()"
               [disabled]="deshabilitado() || bloqueado" placeholder="https://…">
        <button type="button" class="enlace-alternativo" (click)="modoEnlace.set(false)">o sube un archivo</button>
      </div>
    </ng-container>

    <p class="pequeno mt-1" style="color: var(--rojo)" *ngIf="error()">{{ error() }}</p>
  `,
  styles: [`
    .entrada-oculta { position: absolute; width: 1px; height: 1px; opacity: 0; overflow: hidden; }

    .zona-subida {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      padding: 22px 16px;
      border: 2px dashed var(--borde);
      border-radius: var(--radio);
      background: var(--fondo);
      cursor: pointer;
      text-align: center;
      transition: border-color .12s ease, background .12s ease;
    }
    .zona-subida:hover { border-color: var(--gris-claro); }
    .zona-subida.arrastrando { border-color: var(--verde-600); background: var(--verde-50); }
    .zona-subida.deshabilitada { opacity: .55; pointer-events: none; }

    .icono-mas {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 999px;
      background: var(--borde-sutil);
      color: var(--tinta-70);
      font-size: 1.2rem;
      line-height: 1;
      margin-bottom: 4px;
    }
    .zona-subida.arrastrando .icono-mas { background: var(--verde-600); color: #fff; }

    .texto-zona { font-size: .88rem; font-weight: 550; }
    .formatos-zona { font-size: .76rem; color: var(--tinta-70); }

    .enlace-alternativo {
      display: block;
      margin: 6px auto 0;
      background: none;
      border: none;
      padding: 0;
      font-size: .8rem;
      color: var(--tinta-70);
      text-decoration: underline;
      cursor: pointer;
    }

    .campo-enlace { display: flex; align-items: center; gap: 8px; margin-top: 8px; flex-wrap: wrap; }

    .archivo-elegido {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 12px;
      border: 1px solid var(--borde-sutil);
      border-radius: var(--radio);
      background: var(--blanco);
    }
    .miniatura { width: 40px; height: 40px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
    .icono-documento { color: var(--tinta-70); flex-shrink: 0; }
    .nombre-archivo { font-size: .85rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  `],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SubidaArchivoComponent),
    multi: true,
  }],
})
export class SubidaArchivoComponent implements ControlValueAccessor {

  private readonly subidas = inject(SubidasService);

  @Input() tipo: TipoSubida = 'documento';
  /** Además de setDisabledState() (cuando el FormControl está disabled), para bloquearlo desde el propio template. */
  @Input() bloqueado = false;

  readonly valor = signal('');
  readonly subiendo = signal(false);
  readonly error = signal<string | null>(null);
  readonly deshabilitado = signal(false);
  readonly arrastrando = signal(false);
  readonly modoEnlace = signal(false);

  private cambiado: (valor: string) => void = () => {};
  tocado: () => void = () => {};

  get accept(): string {
    return this.tipo === 'imagen' ? 'image/*' : '.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.mp4,image/*';
  }

  get formatosAyuda(): string {
    return this.tipo === 'imagen'
      ? 'JPG, PNG, WEBP o GIF · máx. 5 MB'
      : 'PDF, Word, PowerPoint, Excel, ZIP, vídeo o imagen · máx. 20 MB';
  }

  writeValue(valor: string): void {
    this.valor.set(valor ?? '');
  }

  registerOnChange(fn: (valor: string) => void): void {
    this.cambiado = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.tocado = fn;
  }

  setDisabledState(deshabilitado: boolean): void {
    this.deshabilitado.set(deshabilitado);
  }

  onTouched(): void {
    this.tocado();
  }

  escribir(valor: string): void {
    this.valor.set(valor);
    this.cambiado(valor);
  }

  /** Nombre legible a partir de la URL: el último tramo, sin parámetros de consulta. */
  nombreDe(url: string): string {
    const sinParametros = url.split('?')[0];
    const partes = sinParametros.split('/');
    return decodeURIComponent(partes[partes.length - 1] || url);
  }

  onDragOver(evento: DragEvent): void {
    evento.preventDefault();
    if (!this.subiendo() && !this.deshabilitado() && !this.bloqueado) {
      this.arrastrando.set(true);
    }
  }

  onDragLeave(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrando.set(false);
  }

  onDrop(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrando.set(false);

    if (this.subiendo() || this.deshabilitado() || this.bloqueado) {
      return;
    }
    const archivo = evento.dataTransfer?.files?.[0];
    if (archivo) {
      this.subir(archivo);
    }
  }

  onInputChange(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (archivo) {
      this.subir(archivo);
    }
    input.value = '';
  }

  private subir(archivo: File): void {
    this.subiendo.set(true);
    this.error.set(null);

    this.subidas.subir(archivo, this.tipo).subscribe({
      next: (url) => {
        this.subiendo.set(false);
        this.valor.set(url);
        this.cambiado(url);
        this.tocado();
      },
      error: (err) => {
        this.subiendo.set(false);
        this.error.set(AvisoComponent.mensajeDe(err, 'No se ha podido subir el archivo'));
      },
    });
  }

  quitar(): void {
    this.valor.set('');
    this.cambiado('');
    this.tocado();
  }
}
