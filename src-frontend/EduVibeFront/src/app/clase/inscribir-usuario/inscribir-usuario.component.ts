import { Component, OnInit } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { UserResp } from '../../interfaces/userResp';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-inscribir-usuario',
  templateUrl: './inscribir-usuario.component.html',
  styleUrls: ['./inscribir-usuario.component.css']
})
export class InscribirUsuarioComponent implements OnInit {
  usuarios: UserResp[] = [];
  selectedUser: string = '';
  idClase: number | null = null; // Change to handle null

  constructor(
    private claseService: ClaseService,
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router
  ) {
    const idClase = this.route.snapshot.paramMap.get('idClase');
    if (idClase) {
      this.idClase = +idClase;
    }
  }

  ngOnInit(): void {
    this.obtenerUsuarios();
  }

  obtenerUsuarios(): void {
    this.auth.obtenerUsuarios().subscribe( // Add parentheses to call the method
      (usuarios: UserResp[]) => { // Specify the type of 'usuarios'
        this.usuarios = usuarios;
      },
      error => {
        console.error('Error al obtener usuarios:', error);
      }
    );
  }

  inscribirUsuario(): void {
    if (this.selectedUser && this.idClase !== null) { // Check for null idClase
      this.claseService.inscribirUsuario(this.selectedUser, this.idClase).subscribe(
        () => {
          alert('Usuario inscrito exitosamente');
          this.router.navigate(['/misClases']);
        },
        error => {
          console.error('Error al inscribir usuario:', error);
        }
      );
    }
  }
}
