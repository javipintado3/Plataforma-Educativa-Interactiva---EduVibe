import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserResp } from '../../interfaces/userResp';
import { ActivatedRoute } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-perfil-usuario',
  templateUrl: './perfil-usuario.component.html',
  styleUrl: './perfil-usuario.component.css'
})
export class PerfilUsuarioComponent implements OnInit {
  user: UserResp | null = null;
  userId: number | null = null;

  constructor(private authService: AuthService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.userId = this.authService.getUserId();
    if (this.userId !== null) {
      this.authService.getUserProfile(this.userId).subscribe(
        (data) => {
          this.user = data;
        },
        (error) => {
          console.error('Error fetching user data', error);
        }
      );
    }
  }

  updateProfile(): void {
    if (this.user && this.userId !== null) {
      this.authService.updateUserProfile(this.userId, this.user).subscribe(
        (data) => {
          this.user = data;
          Swal.fire({
            title: "Perfil actualizado",
            text: "Tus datos han sido actualizados exitosamente",
            icon: "success"
          });
        },
        (error) => {
          console.error('Error updating profile', error);
          Swal.fire({
            title: "Error",
            text: "Hubo un problema al actualizar tu perfil",
            icon: "error"
          });
        }
      );
    }
  }
}
