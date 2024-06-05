package com.example.EduvibeBackend.dto;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TareaDTO {
	
	 private String nombreTarea;
	 private String enunciado;
	 private Double calificion;
	 private Boolean estado; 
	
}
