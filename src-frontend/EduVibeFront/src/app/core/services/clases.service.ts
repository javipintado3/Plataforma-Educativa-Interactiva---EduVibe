import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AnaliticaClase, Anuncio, Clase, DetalleClase, DetalleExamen, DetalleHiloForo, DetalleTarea, Entrega, Examen,
  HiloForo, Material, Miembro, ModoVistaClase, Pagina, PreguntaBanco, Subgrupo, Tarea, Tema, TipoMaterial,
} from '../models';
import { TAMANO_MAXIMO_PAGINA, TAMANO_PAGINA, todasLasPaginas } from '../utils/paginacion';

/**
 * Clases y todo lo que cuelga de una.
 *
 * Los servicios no guardan estado: devuelven el observable y quien llama
 * decide qué hacer con él. El único estado compartido de la aplicación es la
 * sesión, y vive en AuthService.
 */
@Injectable({ providedIn: 'root' })
export class ClasesService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/classes`;

  /**
   * Una página de las clases de quien consulta; para la administración, de todas
   * las del centro. La búsqueda por nombre o materia la hace el servidor: filtrar
   * solo lo que hay en la página actual dejaría fuera lo que está en las demás.
   */
  misClases(pagina = 0, busqueda = '', tamano = TAMANO_PAGINA): Observable<Pagina<Clase>> {
    let params = new HttpParams().set('page', pagina).set('size', tamano);
    if (busqueda.trim()) {
      params = params.set('q', busqueda.trim());
    }
    return this.http.get<Pagina<Clase>>(this.api, { params });
  }

  /**
   * Todas las clases de quien consulta, recorriendo las páginas. Para las
   * pantallas que necesitan el conjunto entero (calendario, notas), no una lista
   * que se pinta de diez en diez.
   */
  todasMisClases(): Observable<Clase[]> {
    return todasLasPaginas(pagina => this.misClases(pagina, '', TAMANO_MAXIMO_PAGINA));
  }

  detalle(claseId: string): Observable<DetalleClase> {
    return this.http.get<DetalleClase>(`${this.api}/${claseId}`);
  }

  crear(datos: { name: string; subject?: string; color?: string; imageUrl?: string; viewMode?: ModoVistaClase }): Observable<DetalleClase> {
    return this.http.post<DetalleClase>(this.api, datos);
  }

  actualizar(claseId: string, datos: { name: string; subject?: string; color?: string; imageUrl?: string; viewMode?: ModoVistaClase }): Observable<DetalleClase> {
    return this.http.put<DetalleClase>(`${this.api}/${claseId}`, datos);
  }

  /** Solo administración, y solo si nadie ha entregado nada todavía en la clase. */
  eliminar(claseId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${claseId}`);
  }

  miembros(claseId: string): Observable<Miembro[]> {
    return this.http.get<Miembro[]>(`${this.api}/${claseId}/members`);
  }

  matricular(claseId: string, userId: string, roleInClass: 'teacher' | 'student'): Observable<Miembro> {
    return this.http.post<Miembro>(`${this.api}/${claseId}/members`, { userId, roleInClass });
  }

  desmatricular(claseId: string, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${claseId}/members/${userId}`);
  }

  temas(claseId: string): Observable<Tema[]> {
    return this.http.get<Tema[]>(`${this.api}/${claseId}/topics`);
  }

  crearTema(claseId: string, title: string): Observable<Tema> {
    return this.http.post<Tema>(`${this.api}/${claseId}/topics`, { title });
  }

  actualizarTema(claseId: string, temaId: string, title: string): Observable<Tema> {
    return this.http.put<Tema>(`${this.api}/${claseId}/topics/${temaId}`, { title });
  }

  tareas(claseId: string): Observable<Tarea[]> {
    return this.http.get<Tarea[]>(`${this.api}/${claseId}/assignments`);
  }

  crearTarea(claseId: string, datos: {
    title: string; description?: string; dueDate?: string | null; points?: number;
    latePenaltyPercent?: number; weight?: number; groupAssignment?: boolean; topicId?: string | null;
  }): Observable<DetalleTarea> {
    return this.http.post<DetalleTarea>(`${this.api}/${claseId}/assignments`, datos);
  }

  /** Las entregas propias en esta clase: la pestaña de calificaciones. */
  misEntregas(claseId: string): Observable<Entrega[]> {
    return this.http.get<Entrega[]>(`${this.api}/${claseId}/my-submissions`);
  }

  /** Muro de la clase: fijados primero, luego lo más reciente. */
  avisos(claseId: string, pagina = 0): Observable<Pagina<Anuncio>> {
    const params = new HttpParams().set('page', pagina).set('size', TAMANO_PAGINA);
    return this.http.get<Pagina<Anuncio>>(`${this.api}/${claseId}/announcements`, { params });
  }

  crearAviso(claseId: string, datos: { content: string; pinned?: boolean }): Observable<Anuncio> {
    return this.http.post<Anuncio>(`${this.api}/${claseId}/announcements`, datos);
  }

  /** El aviso ya tiene identidad propia, por eso cuelga de /announcements y no de su clase. */
  borrarAviso(avisoId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/announcements/${avisoId}`);
  }

  materiales(claseId: string): Observable<Material[]> {
    return this.http.get<Material[]>(`${this.api}/${claseId}/resources`);
  }

  crearMaterial(claseId: string, datos: {
    title: string; fileUrl?: string; type?: TipoMaterial | ''; availableFrom?: string | null;
    topicId?: string | null;
  }): Observable<Material> {
    return this.http.post<Material>(`${this.api}/${claseId}/resources`, datos);
  }

  examenes(claseId: string): Observable<Examen[]> {
    return this.http.get<Examen[]>(`${this.api}/${claseId}/exams`);
  }

  crearExamen(claseId: string, datos: {
    title: string;
    description?: string;
    durationMinutes: number;
    dueDate?: string | null;
    topicId?: string | null;
    questions: { text: string; points?: number; options: { text: string; correct: boolean }[] }[];
    reuseQuestions: { questionId: string; points?: number }[];
  }): Observable<DetalleExamen> {
    return this.http.post<DetalleExamen>(`${this.api}/${claseId}/exams`, datos);
  }

  /** Preguntas ya usadas en algún examen de la clase, para reutilizarlas en uno nuevo. */
  bancoDePreguntas(claseId: string): Observable<PreguntaBanco[]> {
    return this.http.get<PreguntaBanco[]>(`${this.api}/${claseId}/exams/question-bank`);
  }

  /** Hilos del foro, con más actividad reciente primero. */
  hilosDeForo(claseId: string): Observable<HiloForo[]> {
    return this.http.get<HiloForo[]>(`${this.api}/${claseId}/forum-threads`);
  }

  abrirHilo(claseId: string, datos: { title: string; content: string; topicId?: string | null }): Observable<DetalleHiloForo> {
    return this.http.post<DetalleHiloForo>(`${this.api}/${claseId}/forum-threads`, datos);
  }

  subgrupos(claseId: string): Observable<Subgrupo[]> {
    return this.http.get<Subgrupo[]>(`${this.api}/${claseId}/groups`);
  }

  crearSubgrupo(claseId: string, datos: { name: string; memberIds: string[] }): Observable<Subgrupo> {
    return this.http.post<Subgrupo>(`${this.api}/${claseId}/groups`, datos);
  }

  /** Panel de analítica: solo lo puede pedir el profesorado de la clase o administración. */
  analitica(claseId: string): Observable<AnaliticaClase> {
    return this.http.get<AnaliticaClase>(`${this.api}/${claseId}/analytics`);
  }
}
