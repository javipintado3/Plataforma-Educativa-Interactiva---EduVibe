package com.example.EduvibeBackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.repository.TareaRepository;
import com.example.EduvibeBackend.service.TareaService;

@Service
public class TareaServiceImpl implements TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Override
    public TareaDTO crearTarea(TareaDTO tareaDto) {
        Tarea tarea = new Tarea();
        tarea.setNombreTarea(tareaDto.getNombreTarea());
        tarea.setEnunciado(tareaDto.getEnunciado());
        Tarea nuevaTarea = tareaRepository.save(tarea);
        return mapToDto(nuevaTarea);
    }

    @Override
    public GetTareaDTO obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id)
                .map(this::mapToGetDto)
                .orElse(null); // O lanzar una excepción personalizada
    }

    @Override
    public TareaDTO editarTarea(Long id, TareaDTO tareaDto) {
        return tareaRepository.findById(id)
                .map(tarea -> {
                    tarea.setNombreTarea(tareaDto.getNombreTarea());
                    tarea.setEnunciado(tareaDto.getEnunciado());
                    return mapToDto(tareaRepository.save(tarea));
                })
                .orElse(null); // O lanzar una excepción personalizada
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
    
    /*
    @Override
    public List<GetTareaDTO> obtenerTareasPorClase(Long idClase) {
        return tareaRepository.findByClaseId(idClase)
                .stream()
                .map(this::mapToGetDto)
                .collect(Collectors.toList());
    }
    */

    private TareaDTO mapToDto(Tarea tarea) {
        TareaDTO tareaDto = new TareaDTO();
        tareaDto.setNombreTarea(tarea.getNombreTarea());
        tareaDto.setEnunciado(tarea.getEnunciado());
        return tareaDto;
    }

    private GetTareaDTO mapToGetDto(Tarea tarea) {
        GetTareaDTO getTareaDto = new GetTareaDTO();
        getTareaDto.setNombreTarea(tarea.getNombreTarea());
        getTareaDto.setEnunciado(tarea.getEnunciado());
        getTareaDto.setFechaApertura(tarea.getFechaApertura());
        return getTareaDto;
    }
}
