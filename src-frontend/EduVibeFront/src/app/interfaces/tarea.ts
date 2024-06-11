import { ClaseDto } from "./clase";
import { UserResp } from "./userResp";

export interface TareaDto {
  idTarea: number;
  nombreTarea: string;
  enunciado: string;
  fechaApertura: Date;
  estado: boolean;
  calificacion: number;
  clase: ClaseDto;
  archivos: any[];  // Asegúrate de definir esta propiedad
  user?: UserResp | null; // Usuario opcional o null
  solucionEscrita:string
}
