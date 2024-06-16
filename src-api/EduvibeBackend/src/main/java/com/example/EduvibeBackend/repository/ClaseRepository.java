package com.example.EduvibeBackend.repository;

import com.example.EduvibeBackend.entities.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Clase
 * Proporciona métodos CRUD para interactuar con la base de datos.
 */
@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    // Esta interfaz hereda métodos CRUD de JpaRepository, como save, findAll, findById, delete, etc.
}
