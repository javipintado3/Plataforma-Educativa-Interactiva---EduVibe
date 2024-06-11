import { Component, OnInit } from '@angular/core';
import { faBell, faMoon } from '@fortawesome/free-solid-svg-icons';
import { AuthService } from '../../services/auth.service';
import { UserResp } from '../../interfaces/userResp';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'] // Corrige el nombre de la propiedad "styleUrl" a "styleUrls"
})
export class NavbarComponent implements OnInit {
  userResp: UserResp | null = null; // Define la propiedad userResp como un objeto de tipo UserResp o nulo

  constructor(public authService: AuthService) {} // Hacer que authService sea público

  ngOnInit(): void {
    this.authService.getIsAuthenticated().subscribe(isAuthenticated => {
      if (isAuthenticated) {
        // Si el usuario está autenticado, obtén los datos del usuario actual
        const userId = this.authService.getUserId();
        if (userId) {
          this.authService.getUserProfile(userId).subscribe(
            (user: UserResp) => this.userResp = user,
            error => console.error('Error fetching user data', error)
          );
        }
      } else {
        this.userResp = null; // Establece userResp en nulo si no hay usuario autenticado
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }

  faMoon = faMoon;
  faBell = faBell;
}
