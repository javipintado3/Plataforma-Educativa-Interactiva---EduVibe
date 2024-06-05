package com.example.EduvibeBackend.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.repository.TareaRepository;
import com.example.EduvibeBackend.service.TareaService;

import jakarta.annotation.Resource;

@Service
public class TareaServiceImpl implements TareaService {

    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private FileStorageService fileStorageService;

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
    
    
 
    public TareaDTO agregarArchivosAdjuntos(Long idTarea, List<MultipartFile> archivosAdjuntos) {
        return tareaRepository.findById(idTarea)
                .map(tarea -> {
                    if (tarea != null) {
                        if (archivosAdjuntos != null && !archivosAdjuntos.isEmpty()) {
                            List<byte[]> bytesArchivos = new ArrayList<>();
                            for (MultipartFile archivo : archivosAdjuntos) {
                                try {
                                    byte[] bytesArchivo = archivo.getBytes();
                                    bytesArchivos.add(bytesArchivo);
                                } catch (IOException e) {
                                    // Manejar la excepción
                                    e.printStackTrace();
                                }
                            }
                            tarea.setArchivoAdjunto(bytesArchivos);
                            tarea = tareaRepository.save(tarea);
                        }
                        return mapToDto(tarea);
                    } else {
                        return null; 
                    }
                })
                .orElse(null); 
    }


    
 
    public List<GetTareaDTO> getTareasByClase(Clase clase) {
        return tareaRepository.findByClase(clase)
                .stream()
                .map(this::mapToGetDto)
                .collect(Collectors.toList());
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
        getTareaDto.setCalificion(tarea.getCalificacion());
        getTareaDto.setEstado(tarea.getEstado());
        
        // Convertir Clase a ClaseDto
        ClaseDto claseDto = mapClaseToDto(tarea.getClase());
        getTareaDto.setClase(claseDto);
        
        return getTareaDto;
    }

    private ClaseDto mapClaseToDto(Clase clase) {
        ClaseDto claseDto = new ClaseDto();
        claseDto.setIdClase(clase.getIdClase());
        claseDto.setNombre(clase.getNombre());
        claseDto.setDescripcion(clase.getDescripcion());
        return claseDto;
    }
}
