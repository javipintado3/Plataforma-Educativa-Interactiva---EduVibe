package com.example.EduvibeBackend.dto;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetTareaDTO {
	 private String nombreTarea;
	 private String enunciado;
	 private LocalDate fechaApertura;
}
