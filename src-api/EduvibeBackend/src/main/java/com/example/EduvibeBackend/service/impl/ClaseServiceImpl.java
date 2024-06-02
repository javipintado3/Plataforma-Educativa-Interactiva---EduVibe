package com.example.EduvibeBackend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.repository.ClaseRepository;
import com.example.EduvibeBackend.service.ClaseService;

@Service
public class ClaseServiceImpl implements ClaseService {

    @Autowired
    private ClaseRepository claseRepository;

    @Override
    public ClaseDto crearClase(ClaseDto claseDto) {
        Clase clase = new Clase();
        
        clase.setNombre(claseDto.getNombre());
        clase.setDescripcion(claseDto.getDescripcion());
        Clase nuevaClase = claseRepository.save(clase);
        return mapToDto(nuevaClase);
    }

    @Override
    public ClaseDto obtenerClasePorId(Long id) {
        Optional<Clase> claseOptional = claseRepository.findById(id);
        if (claseOptional.isPresent()) {
            return mapToDto(claseOptional.get());
        }
        return null; // O lanzar una excepción personalizada
    }

    @Override
    public ClaseDto editarClase(Long id, ClaseDto claseDto) {
        Optional<Clase> claseOptional = claseRepository.findById(id);
        if (claseOptional.isPresent()) {
            Clase claseExistente = claseOptional.get();
            claseExistente.setNombre(claseDto.getNombre());
            claseExistente.setDescripcion(claseDto.getDescripcion());
            Clase claseActualizada = claseRepository.save(claseExistente);
            return mapToDto(claseActualizada);
        }
        return null; // O lanzar una excepción personalizada
    }

    @Override
    public void eliminarClase(Long id) {
        claseRepository.deleteById(id);
    }

    @Override
    public List<ClaseDto> obtenerTodasLasClases() {
        List<Clase> clases = claseRepository.findAll();
        return clases.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ClaseDto mapToDto(Clase clase) {
        ClaseDto claseDto = new ClaseDto();
        claseDto.setIdClase(clase.getIdClase());
        claseDto.setNombre(clase.getNombre());
        claseDto.setDescripcion(clase.getDescripcion());
        return claseDto;
    }
}
