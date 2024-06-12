package com.example.EduvibeBackend.service.impl;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.ClaseRepository;
import com.example.EduvibeBackend.repository.UserRepository;
import com.example.EduvibeBackend.service.ClaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public void inscribirUsuarioEnClase(String email, Long idClase) {
        User usuario = userRepository.findByEmail(email).orElseThrow(() -> new GlobalException("Usuario no encontrado"));
        Clase clase = claseRepository.findById(idClase).orElseThrow(() -> new GlobalException("Clase no encontrada"));

        boolean hayProfesor = clase.getUsuarios().stream()
                .anyMatch(user -> "profesor".equals(user.getRol()));

        boolean usuarioYaInscrito = clase.getUsuarios().stream()
                .anyMatch(user -> user.getEmail().equals(email));

        if (usuarioYaInscrito) {
            throw new IllegalStateException("El usuario ya está inscrito en esta clase.");
        }

        if ("profesor".equals(usuario.getRol()) && hayProfesor) {
            throw new IllegalStateException("Ya hay un profesor inscrito en esta clase.");
        }

        clase.getUsuarios().add(usuario);
        claseRepository.save(clase);
    }

    @Override
    public Clase obtenerClaseSinDto(Long id) {
        return claseRepository.findById(id).orElse(null);
    }

    private ClaseDto mapToDto(Clase clase) {
        ClaseDto claseDto = new ClaseDto();
        claseDto.setIdClase(clase.getIdClase());
        claseDto.setNombre(clase.getNombre());
        claseDto.setDescripcion(clase.getDescripcion());
        return claseDto;
    }

    @Override
    public void eliminarUsuarioDeClase(String email, Long idClase) {
        User usuario = userRepository.findByEmail(email)
            .orElseThrow(() -> new GlobalException("Usuario no encontrado"));
        Clase clase = claseRepository.findById(idClase)
            .orElseThrow(() -> new GlobalException("Clase no encontrada"));

        if (!clase.getUsuarios().contains(usuario)) {
            throw new GlobalException("El usuario no está inscrito en esta clase.");
        }

        clase.getUsuarios().remove(usuario);
        usuario.getClases().remove(clase);
        claseRepository.save(clase);
        userRepository.save(usuario);
    }



}
