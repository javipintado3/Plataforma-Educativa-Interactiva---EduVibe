import { Component } from '@angular/core';
import { faBell, faMoon } from '@fortawesome/free-solid-svg-icons';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  userResp: any; // Define la propiedad userResp
  constructor(public authService: AuthService) {} // Hacer que authService sea público

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.authService.getUserProfile(userId).subscribe(
        data => this.userResp = data,
        error => console.error('Error fetching user data', error)
      );
    }
  }

  logout(){
    this.authService.logout()
  }

  faMoon = faMoon;
  faBell = faBell;
}
