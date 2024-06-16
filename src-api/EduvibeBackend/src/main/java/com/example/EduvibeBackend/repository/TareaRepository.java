package com.example.EduvibeBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.entities.User;

/**
 * Repositorio para la entidad Tarea
 * Proporciona métodos CRUD y consultas personalizadas para interactuar con la base de datos.
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    /**
     * Encuentra una lista de tareas asociadas a una clase específica.
     *
     * @param clase la clase cuyas tareas se quieren encontrar.
     * @return una lista de tareas asociadas a la clase proporcionada.
     */
    List<Tarea> findByClase(Clase clase);

    /**
     * Encuentra una lista de tareas asociadas a una clase y un usuario específicos.
     *
     * @param clase la clase cuyas tareas se quieren encontrar.
     * @param user el usuario cuyas tareas se quieren encontrar.
     * @return una lista de tareas asociadas a la clase y usuario proporcionados.
     */
    List<Tarea> findByClaseAndUsuario(Clase clase, User user);

    /**
     * Encuentra una lista de calificaciones de tareas asociadas a una clase específica.
     *
     * @param idClase el ID de la clase cuyas calificaciones de tareas se quieren encontrar.
     * @return una lista de calificaciones de tareas asociadas a la clase proporcionada.
     */
    @Query("SELECT t.calificacion FROM Tarea t WHERE t.clase.id = :idClase")
    List<Double> findCalificacionesByClaseId(Long idClase);
}
