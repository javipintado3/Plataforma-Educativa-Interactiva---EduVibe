import { Component } from '@angular/core';
import { ClaseService } from '../../services/clase.service';
import { ClaseDto } from '../../interfaces/clase';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-mis-clases',
  templateUrl: './mis-clases.component.html',
  styleUrl: './mis-clases.component.css'
})
export class MisClasesComponent {
  clases: ClaseDto[] = [];


  constructor(private claseService: ClaseService,
    public auth: AuthService
  ) { }

  ngOnInit(): void {
    this.obtenerClases();
  }

  obtenerClases(): void {
    this.claseService.obtenerTodasLasClases().subscribe(clases => {
      this.clases = clases;
    });
  }

  eliminarClase(idClase:number) {

  }

  editarClase(idClase:number) {

  }
 }
