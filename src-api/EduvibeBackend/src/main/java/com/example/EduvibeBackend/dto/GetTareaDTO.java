package com.example.EduvibeBackend.dto;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetTareaDTO {
	 private Long idTarea;
	 private String nombreTarea;
	 private String enunciado;
	 private LocalDate fechaApertura;
	 private Double calificacion;	 private Boolean estado; 
	 private ClaseDto clase;
	 private UsuarioDto user;
}
