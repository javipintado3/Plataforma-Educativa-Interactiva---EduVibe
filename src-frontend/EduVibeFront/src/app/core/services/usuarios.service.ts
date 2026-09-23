import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EstadoCuenta, Invitacion, Pagina, Rol, Usuario, UsuarioCreado } from '../models';

/** Panel de administración de usuarios. */
@Injectable({ providedIn: 'root' })
export class UsuariosService {

  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/users`;

  listar(filtros: {
    role?: Rol | '';
    status?: EstadoCuenta | '';
    q?: string;
    page?: number;
    size?: number;
  } = {}): Observable<Pagina<Usuario>> {

    // Un filtro vacío no se envía: la API entiende la ausencia como "todos"
    let params = new HttpParams();
    if (filtros.role) params = params.set('role', filtros.role);
    if (filtros.status) params = params.set('status', filtros.status);
    if (filtros.q?.trim()) params = params.set('q', filtros.q.trim());
    params = params.set('page', filtros.page ?? 0).set('size', filtros.size ?? 20);

    return this.http.get<Pagina<Usuario>>(this.api, { params });
  }

  crear(datos: { name: string; email: string; role: Rol }): Observable<UsuarioCreado> {
    return this.http.post<UsuarioCreado>(this.api, datos);
  }

  cambiarEstado(usuarioId: string, status: 'active' | 'disabled'): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.api}/${usuarioId}/status`, { status });
  }

  reenviarInvitacion(usuarioId: string): Observable<Invitacion> {
    return this.http.post<Invitacion>(`${this.api}/${usuarioId}/invitation`, {});
  }
}
