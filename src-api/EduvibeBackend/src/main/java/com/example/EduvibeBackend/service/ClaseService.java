package com.example.EduvibeBackend.service;

import java.util.List;

import com.example.EduvibeBackend.dto.ClaseDto;


public interface ClaseService {
	
	ClaseDto crearClase(
			ClaseDto clase);
	

	ClaseDto obtenerClasePorId(Long id);
	

	ClaseDto editarClase(Long id, 
			ClaseDto clase);
	
	void eliminarClase(Long Id);
	
	List<
	ClaseDto> obtenerTodasLasClases();

}
