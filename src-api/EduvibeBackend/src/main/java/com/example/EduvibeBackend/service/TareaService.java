package com.example.EduvibeBackend.service;

import java.util.List;

import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;

/**
 * Interfaz para el servicio de gestión de tareas.
 */
public interface TareaService {
    /**
     * Crea una nueva tarea.
     * 
     * @param tareaDto los datos de la tarea a crear
     * @return el DTO de la tarea creada
     */
    TareaDTO crearTarea(TareaDTO tareaDto);

    /**
     * Obtiene una tarea por su ID.
     * 
     * @param id el ID de la tarea a obtener
     * @return el DTO de la tarea obtenida
     */
    GetTareaDTO obtenerTareaPorId(Long id);

    /**
     * Edita una tarea existente.
     * 
     * @param id el ID de la tarea a editar
     * @param tareaDto los nuevos datos de la tarea
     * @return el DTO de la tarea actualizada
     */
    TareaDTO editarTarea(Long id, TareaDTO tareaDto);

    /**
     * Elimina una tarea por su ID.
     * 
     * @param id el ID de la tarea a eliminar
     */
    void eliminarTarea(Long id);

    /**
     * Obtiene todas las tareas.
     * 
     * @return una lista de DTOs de todas las tareas
     */
    List<GetTareaDTO> obtenerTodasLasTareas();

    /**
     * Crea una nueva tarea y la asigna a una clase específica.
     * 
     * @param idClase el ID de la clase a la que se asignará la tarea
     * @param tareaDto los datos de la tarea a crear
     * @return el DTO de la tarea creada
     */
    TareaDTO crearTareaAsignandoClase(Long idClase, TareaDTO tareaDto);

    /**
     * Asigna una tarea a un usuario específico.
     * 
     * @param idTarea el ID de la tarea a asignar
     * @param idUsuario el ID del usuario al que se asignará la tarea
     */
    void asignarTareaAUsuario(Long idTarea, Integer idUsuario);

    /**
     * Edita la calificación de una tarea.
     * 
     * @param idTarea el ID de la tarea a calificar
     * @param nuevaCalificacion la nueva calificación a asignar
     */
    void editarCalificacionTarea(Long idTarea, Double nuevaCalificacion);

    /**
     * Edita la solución escrita de una tarea.
     * 
     * @param id el ID de la tarea a editar
     * @param nuevaSolucionEscrita la nueva solución escrita de la tarea
     * @return el DTO de la tarea actualizada
     */
    TareaDTO editarSolucionEscrita(Long id, String nuevaSolucionEscrita);

    /**
     * Obtiene las tareas de una clase específica para un usuario específico.
     * 
     * @param clase la clase de la cual se obtendrán las tareas
     * @param user el usuario para el cual se obtendrán las tareas
     * @return una lista de DTOs de las tareas
     */
    List<GetTareaDTO> getTareasByClaseAndUser(Clase clase, User user);

    /**
     * Obtiene las tareas de una clase específica.
     * 
     * @param clase la clase de la cual se obtendrán las tareas
     * @return una lista de DTOs de las tareas
     */
    List<GetTareaDTO> getTareasByClase(Clase clase);

    /**
     * Calcula la media de las calificaciones de una clase específica.
     * 
     * @param idClase el ID de la clase para la cual se calculará la media de las calificaciones
     * @return la media de las calificaciones
     */
    Double calcularMediaCalificaciones(Long idClase);
}
