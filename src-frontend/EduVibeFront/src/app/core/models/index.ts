/**
 * Tipos que devuelve la API.
 *
 * Están en un único fichero porque son declaraciones puras, sin lógica, y
 * repartirlas en diez ficheros de seis líneas solo añadiría importaciones.
 * Se corresponden uno a uno con los DTO del backend.
 */

export type Rol = 'admin' | 'teacher' | 'student' | 'guardian';
export type EstadoCuenta = 'pending' | 'active' | 'disabled';
export type RolEnClase = 'teacher' | 'student';
export type EstadoEntrega = 'draft' | 'submitted' | 'graded';

export interface Usuario {
  id: string;
  email: string;
  name: string;
  role: Rol;
  status: EstadoCuenta;
  avatarUrl: string | null;
  organizationId: string;
  createdAt: string | null;
}

export interface RespuestaAutenticacion {
  token: string;
  expiresAt: string;
  user: Usuario;
}

export interface Invitacion {
  link: string;
  expiresAt: string;
  emailEnviado: boolean;
}

export interface DatosInvitacion {
  name: string;
  email: string;
  organizationName: string;
  expiresAt: string;
}

export interface UsuarioCreado {
  user: Usuario;
  invitation: Invitacion;
}

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
  ultima: boolean;
}

export interface Clase {
  id: string;
  name: string;
  subject: string | null;
  color: string | null;
  imageUrl: string | null;
  miRol: RolEnClase | 'admin' | null;
  proximaEntrega: string | null;
  profesores: string[];
  createdAt: string | null;
}

export interface Miembro {
  userId: string;
  name: string;
  email: string;
  roleInClass: RolEnClase;
  enrolledAt: string | null;
}

export interface Tema {
  id: string;
  title: string;
  sortOrder: number;
}

export interface DetalleClase {
  id: string;
  name: string;
  subject: string | null;
  color: string | null;
  imageUrl: string | null;
  miRol: RolEnClase | 'admin' | null;
  puedoEditar: boolean;
  profesores: Miembro[];
  numeroAlumnos: number;
  temas: Tema[];
}

export interface Tarea {
  id: string;
  classId: string;
  topicId: string | null;
  title: string;
  dueDate: string | null;
  points: number;
  haVencido: boolean;
  miEstado: EstadoEntrega | null;
  miEntregaTarde: boolean | null;
  entregasRecibidas: number | null;
  createdAt: string | null;
}

export interface DetalleTarea {
  id: string;
  classId: string;
  className: string;
  classSubject: string | null;
  topicId: string | null;
  title: string;
  description: string | null;
  dueDate: string | null;
  points: number;
  haVencido: boolean;
  puedoEditar: boolean;
  miEntrega: Entrega | null;
  createdAt: string | null;
}

export interface Calificacion {
  score: number;
  feedback: string | null;
  gradedByName: string;
  gradedAt: string;
}

export interface Entrega {
  id: string;
  assignmentId: string;
  assignmentTitle: string;
  studentId: string;
  studentName: string;
  content: string | null;
  fileUrl: string | null;
  status: EstadoEntrega;
  submittedAt: string | null;
  entregadaTarde: boolean;
  grade: Calificacion | null;
}

export interface Anuncio {
  id: string;
  content: string;
  pinned: boolean;
  authorName: string;
  createdAt: string;
  /** Lo decide el servicio, no el cliente: solo aparece a quien puede borrarlo. */
  puedoBorrar: boolean;
}

export type TipoMaterial = 'pdf' | 'link' | 'video' | 'doc' | 'other';

export interface Material {
  id: string;
  topicId: string | null;
  title: string;
  fileUrl: string | null;
  type: TipoMaterial | null;
  sortOrder: number;
  createdAt: string | null;
}

export type TipoEventoAgenda = 'exam' | 'holiday' | 'other';
export type OrigenEntradaAgenda = 'event' | 'assignment';

/**
 * Una entrada de la agenda: un evento a mano o una fecha de entrega derivada.
 * `tipo` puede ser un TipoEventoAgenda o 'assignment_due' cuando el origen es
 * una tarea.
 */
export interface EntradaAgenda {
  referencia: string;
  origen: OrigenEntradaAgenda;
  title: string;
  fecha: string;
  tipo: string;
  classId: string | null;
  className: string | null;
  classSubject: string | null;
  classColor: string | null;
}

export interface Notificacion {
  id: string;
  type: string;
  texto: string;
  payload: Record<string, unknown> | null;
  leida: boolean;
  createdAt: string;
}

/**
 * Resumen de la pantalla de perfil.
 *
 * Un único tipo para los tres roles: cada uno rellena solo los campos que le
 * corresponden, el resto llega a null. Así lo devuelve la API.
 */
export interface ResumenPerfil {
  role: Rol;
  numeroClases: number | null;
  numeroAlumnos: number | null;
  entregasPorCorregir: number | null;
  notaMedia: number | null;
  tareasPendientes: number | null;
  notificacionesNoLeidas: number | null;
  usuariosPorRol: Record<string, number> | null;
  invitacionesPendientes: number | null;
}

/** Forma de los errores que devuelve la API. */
export interface ErrorApi {
  status: number;
  error: string;
  message: string;
  path: string;
  campos?: Record<string, string>;
  timestamp: string;
}
