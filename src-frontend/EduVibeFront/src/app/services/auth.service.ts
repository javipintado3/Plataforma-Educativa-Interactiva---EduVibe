import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { LoginRequest } from '../request/loginRequest';
import { UserResp } from '../interfaces/userResp';
import {jwtDecode} from 'jwt-decode';
import { useAnimation } from '@angular/animations';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:9090';
  private isAuthenticated$: BehaviorSubject<boolean>;
  name = ""

  constructor(private http: HttpClient, private router: Router) {
    this.isAuthenticated$ = new BehaviorSubject<boolean>(this.isAuthenticated());
  }

  private isLocalStorageAvailable(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  login(loginData: LoginRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/loginuser`, loginData)
      .pipe(
        tap(response => {
          if (this.isLocalStorageAvailable()) {
            localStorage.setItem('token', response.token);
            const decodedToken: any = jwtDecode(response.token);
            localStorage.setItem('name', decodedToken.name); // Almacena el nombre de usuario
          }
          this.isAuthenticated$.next(true);
          // Mostrar alerta exitosa
        }),
        catchError(error => {
          // Mostrar alerta de error
          Swal.fire({
            title: "Error",
            text: error.message,
            icon: "error"
          });
          return throwError(error);
        })
      );
  }

  logout(): void {
    if (this.isLocalStorageAvailable()) {
      localStorage.removeItem('token'); // Elimina el token del LocalStorage
    }
    this.isAuthenticated$.next(false);
    Swal.fire({
      title: "Sesión cerrada",
      icon: "success"
    });
    this.router.navigate(['/login']); // Redirige al login
  }

  isLogged(): boolean {
    return this.isLocalStorageAvailable() && !!localStorage.getItem("token");
  }

  register(registerData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/registeruser`, registerData);
  }

  getToken(): string | null {
    return this.isLocalStorageAvailable() ? localStorage.getItem('token') : null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getIsAuthenticated(): Observable<boolean> {
    return this.isAuthenticated$.asObservable();
  }

  validate(): Observable<boolean> {
    return this.http.get<UserResp>(`${this.apiUrl}/validate`)
      .pipe(
        map(resp => true),
        catchError(err => {
          console.log(err.error.message);
          return of(false);
        })
      );
  }

  renew(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/renew`);
  }

  /*
  minutosRestantes(): number {
    let minutosRestantes = -1;
    if (this.isLocalStorageAvailable() && localStorage.getItem("token")) {
      const expiracion: number = jwtDecode(localStorage.getItem("token") || "").exp as any;
      const dateExp = new Date(expiracion * 1000);
      const now = new Date();
      if (((dateExp.getTime() - now.getTime()) / (1000 * 60)) > 0) {
        minutosRestantes = Math.abs(dateExp.getTime() - now.getTime()) / (1000 * 60);
      }
    }
    return minutosRestantes;
  }
  */

  ifAdmin(): boolean {
    return (this.getUserData()?.role=="admin")? true:false
  }

  getUserData() {
    let token:string = localStorage.getItem("token") as any;
    const {name, role} = jwtDecode(token) as any
    return {
      nombre: name,
      role: role
    }
    
  }
  
  
}
