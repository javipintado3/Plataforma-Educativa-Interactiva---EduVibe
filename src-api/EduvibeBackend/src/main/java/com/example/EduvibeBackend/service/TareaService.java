package com.example.EduvibeBackend.service;

import java.util.List;

import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;


public interface TareaService {
    TareaDTO crearTarea(TareaDTO tareaDto);
    GetTareaDTO obtenerTareaPorId(Long id);
    TareaDTO editarTarea(Long id, TareaDTO tareaDto);
    void eliminarTarea(Long id);
    List<GetTareaDTO> obtenerTodasLasTareas();
    
    /*List<GetTareaDTO> obtenerTareasPorClase(Long idClase); */ // Método para listar tareas por clase

}
