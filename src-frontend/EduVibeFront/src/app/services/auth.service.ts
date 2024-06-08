import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { LoginRequest } from '../request/loginRequest';
import { UserResp } from '../interfaces/userResp';
import { jwtDecode } from 'jwt-decode';
import { useAnimation } from '@angular/animations';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:9090';
  private isAuthenticated$: BehaviorSubject<boolean>;
  name = "";
  rol = ""; // Cambio 'role' por 'rol'

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
            localStorage.setItem('rol', decodedToken.rol); // Almacena el rol
            localStorage.setItem('id', decodedToken.id); // Almacena el rol
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

  ifAdmin(): boolean {
    return (this.getUserData()?.rol == "admin") ? true : false; // Cambio 'role' por 'rol'
  }
  ifAlumno(): boolean {
    return (this.getUserData()?.rol == "alumno") ? true : false; // Cambio 'role' por 'rol'
  }
  ifProfesor(): boolean {
    return (this.getUserData()?.rol == "profesor") ? true : false; // Cambio 'role' por 'rol'
  }


  getUserData() {
    let token: string = localStorage.getItem("token") as any;
    const { name, rol, id } = jwtDecode(token) as any;
    return {
      nombre: name,
      rol: rol, 
      id: id
    };

  }

  //metodo para calcular los minutos restantes del token
  minutosRestantes(){
    //creamos la variable
    let minutosRestantes = -1
    //si hay token en el localstorage entocnes entra
    if(localStorage.getItem("token")){
      //sacamos los milisegundos del token
      let expiracion:number = jwtDecode((localStorage.getItem("token") || "") ).exp as any
      //lo seteamos en una nueva fecha
      let dateExp = new Date(expiracion*1000)
      //creamos una fecha de hoy
      let now = new Date()
      //si el tiempo que hay de diferencia en minutos es mayor que 0 entra
      if(((dateExp.getTime()-now.getTime())/(1000*60))>0){
        //sacamos el valor absoluto entre la fecha de expiracion y hoy, en minutos
        minutosRestantes = Math.abs(dateExp.getTime()-now.getTime())/(1000*60)
      }else{
        //si no pues seguimos dejando la variable por defecto
        minutosRestantes = -1
      }
      console.log(minutosRestantes)
    }
    // devolvemos los minutos
    return minutosRestantes
  }
  
  // Método para obtener el ID del usuario del token decodificado
  getUserId(): number | null {
    const token = localStorage.getItem('token');
    if (token) {
      const decodedToken: any = jwtDecode(token);
      return decodedToken.id; // Cambia 'id' por el nombre correcto del campo en el token
    }
    return null;
  }

  obtenerUsuariosPorClase(idClase: number): Observable<UserResp[]> {
    return this.http.get<UserResp[]>(`${this.apiUrl}/usuarios/clase/${idClase}`).pipe(
      catchError(error => {
        console.error('Error al obtener usuarios de la clase', error);
        return throwError(error);
      })
    );
  }

    // Método para obtener los datos del usuario
    getUserProfile(id: number): Observable<UserResp> {
      return this.http.get<UserResp>(`${this.apiUrl}/user/${id}`);
    }
  
    // Método para actualizar los datos del usuario
    updateUserProfile(id: number, userData: Partial<UserResp>): Observable<UserResp> {
      return this.http.put<UserResp>(`${this.apiUrl}/usuarios/${id}`, userData);
    }
  
   
  // Método para obtener todos los usuarios
  obtenerUsuarios(): Observable<UserResp[]> {
    return this.http.get<UserResp[]>(`${this.apiUrl}/usuarios`);
  }

}
