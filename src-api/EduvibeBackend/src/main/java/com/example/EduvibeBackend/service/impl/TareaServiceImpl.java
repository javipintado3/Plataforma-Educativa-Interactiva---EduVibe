package com.example.EduvibeBackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.dto.UsuarioDto;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.TareaRepository;
import com.example.EduvibeBackend.repository.UserRepository;
import com.example.EduvibeBackend.service.TareaService;

/**
 * Implementación del servicio de gestión de tareas.
 */
@Service
public class TareaServiceImpl implements TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ClaseServiceImpl claseService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public TareaDTO crearTarea(TareaDTO tareaDto) {
        Tarea tarea = new Tarea();
        tarea.setNombreTarea(tareaDto.getNombreTarea());
        tarea.setEnunciado(tareaDto.getEnunciado());
        tarea.setEstado(false);
        Tarea nuevaTarea = tareaRepository.save(tarea);
        return mapToDto(nuevaTarea);
    }

    @Override
    public TareaDTO crearTareaAsignandoClase(Long idClase, TareaDTO tareaDto) {
        ClaseDto claseDto = claseService.obtenerClasePorId(idClase);
        
        if (claseDto != null) {
            Tarea tarea = new Tarea();
            tarea.setNombreTarea(tareaDto.getNombreTarea());
            tarea.setEnunciado(tareaDto.getEnunciado());
            tarea.setEstado(false);
            
            Clase clase = new Clase();
            clase.setIdClase(claseDto.getIdClase());
            clase.setNombre(claseDto.getNombre());
            clase.setDescripcion(claseDto.getDescripcion());
            tarea.setClase(clase);
            
            Tarea nuevaTarea = tareaRepository.save(tarea);
            return mapToDto(nuevaTarea);
        } else {
            throw new GlobalException("La clase con ID " + idClase + " no fue encontrada.");
        }
    }

    @Override
    public GetTareaDTO obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id)
                .map(this::mapToGetDto)
                .orElse(null);
    }

    @Override
    public TareaDTO editarTarea(Long id, TareaDTO tareaDto) {
        return tareaRepository.findById(id)
                .map(tarea -> {
                    tarea.setNombreTarea(tareaDto.getNombreTarea());
                    tarea.setEnunciado(tareaDto.getEnunciado());
                    return mapToDto(tareaRepository.save(tarea));
                })
                .orElse(null);
    }

    @Override
    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    @Override
    public List<GetTareaDTO> obtenerTodasLasTareas() {
        return tareaRepository.findAll()
                .stream()
                .map(this::mapToGetDto)
                .collect(Collectors.toList());
    }

    @Override
    public void asignarTareaAUsuario(Long idTarea, Integer idUsuario) {
        User usuario = userRepository.findById(idUsuario)
                                     .orElseThrow(() -> new GlobalException("El usuario con ID " + idUsuario + " no fue encontrado."));

        Tarea tarea = tareaRepository.findById(idTarea)
                                     .orElseThrow(() -> new GlobalException("La tarea con ID " + idTarea + " no fue encontrada."));

        usuario.getTareas().add(tarea);
        tarea.setUsuario(usuario);

        userRepository.save(usuario);
        tareaRepository.save(tarea);
    }

    @Override
    public void editarCalificacionTarea(Long idTarea, Double nuevaCalificacion) {
        if (nuevaCalificacion < 0 || nuevaCalificacion > 10) {
            throw new GlobalException("La calificación debe estar en el rango de 0 a 10.");
        }

        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new GlobalException("La tarea con ID " + idTarea + " no fue encontrada."));

        tarea.setCalificacion(nuevaCalificacion);
        tareaRepository.save(tarea);
    }

    @Override
    public TareaDTO editarSolucionEscrita(Long id, String nuevaSolucionEscrita) {
        return tareaRepository.findById(id)
                .map(tarea -> {
                    tarea.setSolucionEscrita(nuevaSolucionEscrita);
                    return mapToDto(tareaRepository.save(tarea));
                })
                .orElse(null);
    }

    @Override
    public List<GetTareaDTO> getTareasByClaseAndUser(Clase clase, User user) {
        return tareaRepository.findByClaseAndUsuario(clase, user)
                .stream()
                .map(this::mapToGetDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetTareaDTO> getTareasByClase(Clase clase) {
        return tareaRepository.findByClase(clase)
                .stream()
                .map(this::mapToGetDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double calcularMediaCalificaciones(Long idClase) {
        List<Double> calificaciones = tareaRepository.findCalificacionesByClaseId(idClase);
        if (calificaciones.isEmpty()) {
            return null;
        }
        double sum = 0;
        for (Double calificacion : calificaciones) {
            sum += calificacion;
        }
        return sum / calificaciones.size();
    }

    private TareaDTO mapToDto(Tarea tarea) {
        TareaDTO tareaDto = new TareaDTO();
        tareaDto.setNombreTarea(tarea.getNombreTarea());
        tareaDto.setEnunciado(tarea.getEnunciado());
        return tareaDto;
    }

    private GetTareaDTO mapToGetDto(Tarea tarea) {
        GetTareaDTO getTareaDto = new GetTareaDTO();
        getTareaDto.setIdTarea(tarea.getIdTarea());
        getTareaDto.setNombreTarea(tarea.getNombreTarea());
        getTareaDto.setEnunciado(tarea.getEnunciado());
        getTareaDto.setFechaApertura(tarea.getFechaApertura());
        getTareaDto.setCalificacion(tarea.getCalificacion());
        getTareaDto.setEstado(tarea.getEstado());
        getTareaDto.setSolucionEscrita(tarea.getSolucionEscrita());

        ClaseDto claseDto = mapClaseToDto(tarea.getClase());
        getTareaDto.setClase(claseDto);
        UsuarioDto userDto = mapUserToDto(tarea.getUsuario());
        getTareaDto.setUser(userDto);

        return getTareaDto;
    }

    private ClaseDto mapClaseToDto(Clase clase) {
        ClaseDto claseDto = new ClaseDto();
        claseDto.setIdClase(clase.getIdClase());
        claseDto.setNombre(clase.getNombre());
        claseDto.setDescripcion(clase.getDescripcion());
        return claseDto;
    }

    private UsuarioDto mapUserToDto(User user) {
        UsuarioDto userDto = new UsuarioDto();
        if (user != null) {
            userDto.setId(user.getId());
            userDto.setNombre(user.getNombre());
            userDto.setEmail(user.getEmail());
        }
        return userDto;
    }
}
