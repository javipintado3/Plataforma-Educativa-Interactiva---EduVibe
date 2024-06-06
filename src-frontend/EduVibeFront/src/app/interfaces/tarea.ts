import { ClaseDto } from "./clase";

export interface TareaDto {
  idTarea: number;
  nombreTarea: string;
  enunciado: string;
  fechaApertura: Date;
  estado: boolean;
  calificacion: number;
  clase: ClaseDto;
  archivos: any[];  // Asegúrate de definir esta propiedad
}
