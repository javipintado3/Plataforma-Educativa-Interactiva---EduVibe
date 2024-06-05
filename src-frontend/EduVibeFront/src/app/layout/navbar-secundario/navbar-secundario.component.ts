import { Component, Input } from '@angular/core';
import { ClaseDto } from '../../interfaces/clase';

@Component({
  selector: 'app-navbar-secundario',
  templateUrl: './navbar-secundario.component.html',
  styleUrl: './navbar-secundario.component.css'
})
export class NavbarSecundarioComponent {
  @Input() clase: ClaseDto | undefined;

}
