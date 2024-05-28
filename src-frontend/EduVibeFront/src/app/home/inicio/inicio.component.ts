import { Component } from '@angular/core';
import { ClaseDto } from '../../interfaces/clase';
import { ClaseService } from '../../services/clase.service';

@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css'
})
export class InicioComponent {

  clases: ClaseDto[] = [];

  constructor(private claseService: ClaseService) { }

  ngOnInit(): void {
    this.obtenerClases();
  }

  obtenerClases(): void {
    this.claseService.obtenerTodasLasClases().subscribe(clases => {
      this.clases = clases;
    });
  }
}
