package com.example.EduvibeBackend.dto;

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
public class ClaseDto {
	
	private Long idClase;
	
	private String nombre;
	
	private String descripcion;

}
