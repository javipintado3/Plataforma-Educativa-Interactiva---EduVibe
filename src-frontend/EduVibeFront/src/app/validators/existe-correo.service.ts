import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AbstractControl, AsyncValidator, ValidationErrors } from '@angular/forms';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ExisteCorreoService implements AsyncValidator{

constructor(private http: HttpClient) { }

  validate(control: AbstractControl): Observable<ValidationErrors | null> {
    const email = control.value;
    console.log(email)

    return this.http.get<any[]>(`${environment.apiUrl}/user/existeEmail?email=${email}`)
    .pipe(
      map( resp => {
        return (resp.length === 0) ? null : { emailTaken: true}
      }),
      catchError(error => of(null))

    )
    
  }
}
