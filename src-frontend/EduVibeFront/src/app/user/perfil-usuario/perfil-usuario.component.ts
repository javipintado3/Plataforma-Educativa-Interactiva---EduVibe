import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-perfil-usuario',
  templateUrl: './perfil-usuario.component.html',
  styleUrl: './perfil-usuario.component.css'
})
export class PerfilUsuarioComponent {
  userData: any;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.userData = this.authService.getUserData();
  }
}
