import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';


import { jwtDecode } from 'jwt-decode';


import Swal from 'sweetalert2';
import { LoginRequest } from '../request/loginRequest';
import { UserResp } from '../interfaces/userResp';


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



  login(loginData:LoginRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/loginuser`,loginData)
      .pipe(  //Operador para manipular datos emitidos por el observable 
        tap(  //Tap hace estas operaciones si se recibe una respuesta exitosa
          response => { 
            localStorage.setItem('token', response.token);
            this.isAuthenticated$.next(true);

            localStorage.setItem('token', response.token);
            const decodedToken: any = jwtDecode(response.token);
            localStorage.setItem('name', decodedToken.name); // Almacena el nombre de usuario
            this.isAuthenticated$.next(true);

          
            //Mostrar alerta exitosa
        }),
        catchError(
          error => {
            //Mostrar alerta de error
            return error;
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token'); //eliminame el token del LocalStorage
    this.isAuthenticated$.next(false);                      
    Swal.fire({ // muestra una alert exitosa aquí
      title: "Cesion cerrada",
      icon: "success"
    })
    this.router.navigate(['/login']); //Devuelveme al login
  }

  isLogged():boolean{
    return localStorage.getItem("token") ? true:false
  }

  register(registerData:any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/registeruser`,registerData);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getIsAuthenticated(): Observable<boolean> {
    return this.isAuthenticated$.asObservable();
  }

  //Metodo que manda una peticion get al back para validar que el token 
  //siga siendo valido.
  validate(){
   return this.http.get<UserResp>(`${this.apiUrl}/validate`)
    .pipe(
      map(resp=>{
        //si hay respuesta manda true
        return true
      }),
      catchError(err=>{
        //si hay un error manda false y lo pintamos por consola
        console.log(err.error.message)
        return of(false)
      })
    )
  }

  renew():Observable<any>{
    return this.http.get<any>(`${this.apiUrl}/renew`)
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
  
  ifAdmin(){ // comprueba si eres admin
    return (this.getUserData().role == "admin")? true : false;
  }

  getUserData(){ // devuelve el tipo de rol y el nombre de usuario del token
    let token:string = localStorage.getItem("token") as any;
    const {name, role, username} = jwtDecode(token) as any
    return {
      nombre: name,
      role: role,
      username: username
    }
  }
 
}
