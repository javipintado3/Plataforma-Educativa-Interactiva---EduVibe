import { Component } from '@angular/core';
import { faBell, faMoon } from '@fortawesome/free-solid-svg-icons';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  constructor(private authService:AuthService){}

  logout(){
    this.authService.logout()
  }

  faMoon = faMoon;
  faBell = faBell;
}
