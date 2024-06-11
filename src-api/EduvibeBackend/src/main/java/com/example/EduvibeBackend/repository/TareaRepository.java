package com.example.EduvibeBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.entities.User;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
	
	
    List<Tarea> findByClase(Clase clase);
    List<Tarea> findByClaseAndUsuario(Clase clase, User user); // Agrega este método
    @Query("SELECT t.calificacion FROM Tarea t WHERE t.clase.id = :idClase")
    List<Double> findCalificacionesByClaseId(Long idClase);

}

