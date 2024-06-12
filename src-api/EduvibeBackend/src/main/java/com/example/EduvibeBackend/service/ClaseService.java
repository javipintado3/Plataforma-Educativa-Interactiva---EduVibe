package com.example.EduvibeBackend.service;

import java.util.List;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.entities.Clase;

/**
 * Servicio para gestionar las operaciones relacionadas con las clases.
 */
public interface ClaseService {
    
    /**
     * Crea una nueva clase.
     * 
     * @param claseDto La información de la clase a crear.
     * @return La clase creada.
     */
    ClaseDto crearClase(ClaseDto claseDto);
    
    /**
     * Obtiene una clase por su identificador único.
     * 
     * @param id El identificador único de la clase.
     * @return La clase encontrada, o null si no se encontró ninguna clase con el ID dado.
     */
    ClaseDto obtenerClasePorId(Long id);
    
    /**
     * Edita una clase existente.
     * 
     * @param id El identificador único de la clase a editar.
     * @param claseDto La información actualizada de la clase.
     * @return La clase actualizada.
     */
    ClaseDto editarClase(Long id, ClaseDto claseDto);
    
    /**
     * Elimina una clase por su identificador único.
     * 
     * @param id El identificador único de la clase a eliminar.
     */
    void eliminarClase(Long id);
    
    /**
     * Obtiene todas las clases disponibles.
     * 
     * @return Una lista de todas las clases.
     */
    List<ClaseDto> obtenerTodasLasClases();
    
    /**
     * Obtiene una clase por su identificador único, sin mapear a un DTO.
     * 
     * @param id El identificador único de la clase.
     * @return La clase encontrada, o null si no se encontró ninguna clase con el ID dado.
     */
    Clase obtenerClaseSinDto(Long id);
    
    /**
     * Inscribir a un usuario en una clase.
     * 
     * @param email El correo electrónico del usuario a inscribir.
     * @param idClase El identificador único de la clase a la que se inscribirá el usuario.
     */
    void inscribirUsuarioEnClase(String email, Long idClase);
    
    /**
     * Elimina a un usuario de una clase.
     * 
     * @param email El correo electrónico del usuario a eliminar.
     * @param idClase El identificador único de la clase de la que se eliminará el usuario.
     */
    void eliminarUsuarioDeClase(String email, Long idClase);
    
    /**
     * Obtiene todas las clases a las que está inscrito un usuario.
     * 
     * @param idUsuario El identificador único del usuario.
     * @return Una lista de todas las clases a las que está inscrito el usuario.
     */
    List<ClaseDto> obtenerClasesPorUsuario(Integer idUsuario);
}
