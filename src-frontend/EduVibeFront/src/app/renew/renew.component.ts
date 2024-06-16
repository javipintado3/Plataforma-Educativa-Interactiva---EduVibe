import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-renew',
  templateUrl: './renew.component.html'
})
export class RenewComponent implements OnInit{

  constructor(private authService:AuthService,
              private router:Router
  ){}

  ngOnInit(): void {
    this.authService.renew()
    .subscribe({
      next:token=>{
        console.log(token)
        localStorage.setItem("token", token.token)
        Swal.fire({
          title:"Sesion renovada correctamente",
          icon:"success"
        })
        this.router.navigateByUrl("/inicio")
        
      },
      error:err=>{
      Swal.fire({
        title:"Error",
        icon:"success",
        text: err.error.message
      })
      }
    })
  }

}
