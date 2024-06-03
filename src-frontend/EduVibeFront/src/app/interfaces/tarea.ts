import { ClaseDto } from "./clase";

export interface TareaDto {
  idTarea: number;
  nombreTarea: string;
  enunciado: string;
  fechaApertura: Date; // O puedes usar string si lo prefieres, dependiendo de cómo venga del backend
  clase: ClaseDto;
  }
  