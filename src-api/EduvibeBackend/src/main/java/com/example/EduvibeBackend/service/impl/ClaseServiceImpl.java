package com.example.EduvibeBackend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.ClaseRepository;
import com.example.EduvibeBackend.repository.UserRepository;
import com.example.EduvibeBackend.service.ClaseService;

import io.jsonwebtoken.lang.Collections;

@Service
public class ClaseServiceImpl implements ClaseService {

    @Autowired
    private ClaseRepository claseRepository;
    
    @Autowired
    private UserRepository userRepository;

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
    

    public List<ClaseDto> obtenerClasesPorUsuario(Integer idUsuario) {
        User usuario = userRepository.findById(idUsuario)
                                      .orElseThrow(() -> new GlobalException("Usuario no encontrado"));

        List<Clase> clases = userRepository.findClasesByUserId(idUsuario);

        return clases.stream()
                     .map(this::mapToDto)
                     .collect(Collectors.toList());
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
    

    public void inscribirUsuarioEnClase(String email, Long idClase) {
    	User usuario = userRepository.findByEmail(email).orElse(null);
        Clase clase = claseRepository.findById(idClase).orElse(null);
        if (usuario != null && clase != null) {
            usuario.getClases().add(clase);
            userRepository.save(usuario);
        }
    }
    
    public Clase obtenerClaseSinDto(Long id)  {
    	 Clase claseOptional = claseRepository.findById(id).orElse(null);
		return claseOptional;
    }

    private ClaseDto mapToDto(Clase clase) {
        ClaseDto claseDto = new ClaseDto();
        claseDto.setIdClase(clase.getIdClase());
        claseDto.setNombre(clase.getNombre());
        claseDto.setDescripcion(clase.getDescripcion());
        return claseDto;
    }
    
    
}
