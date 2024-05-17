package com.example.EduvibeBackend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.repository.ClaseRepository;
import com.example.EduvibeBackend.service.ClaseService;

@Service
public class ClaseServiceImpl implements ClaseService {
	
	@Autowired
	private ClaseRepository claseRepository;

	@Override
	public ClaseDto crearClase(ClaseDto clase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClaseDto obtenerClasePorId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClaseDto editarClase(Long id, ClaseDto clase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void eliminarClase(Long Id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ClaseDto> obtenerTodasLasClases() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	

	
	

}
