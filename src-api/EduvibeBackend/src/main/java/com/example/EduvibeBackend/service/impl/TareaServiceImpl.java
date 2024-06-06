package com.example.EduvibeBackend.service.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.TareaRepository;
import com.example.EduvibeBackend.service.TareaService;

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
    
    
 
 // Método para agregar archivo a una tarea
    public void agregarArchivoAdjunto(Long idTarea, MultipartFile archivo) throws IOException, SQLException {
        Optional<Tarea> optionalTarea = tareaRepository.findById(idTarea);
        if (optionalTarea.isPresent()) {
            Tarea tarea = optionalTarea.get();
            Blob archivoBlob = new SerialBlob(archivo.getBytes());
            tarea.getArchivoAdjunto().add(archivoBlob);
            tareaRepository.save(tarea);
        } else {
            throw new GlobalException("Tarea no encontrada con el ID proporcionado: " + idTarea);
        }
    }

    // Método para descargar archivo de una tarea
    public byte[] descargarArchivoAdjunto(Long idTarea, int indiceArchivo) throws SQLException {
        Optional<Tarea> optionalTarea = tareaRepository.findById(idTarea);
        if (optionalTarea.isPresent()) {
            Tarea tarea = optionalTarea.get();
            if (indiceArchivo >= 0 && indiceArchivo < tarea.getArchivoAdjunto().size()) {
                Blob archivoBlob = tarea.getArchivoAdjunto().get(indiceArchivo);
                return archivoBlob.getBytes(1, (int) archivoBlob.length());
            } else {
                throw new GlobalException("Índice de archivo inválido: " + indiceArchivo);
            }
        } else {
            throw new GlobalException("Tarea no encontrada con el ID proporcionado: " + idTarea);
        }
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
