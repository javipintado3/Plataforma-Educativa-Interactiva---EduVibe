package com.example.EduvibeBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;

/**
 * Repositorio para la entidad User
 * Proporciona métodos CRUD y consultas personalizadas para interactuar con la base de datos.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Verifica si existe un usuario con el correo electrónico especificado, ignorando el caso.
     *
     * @param email el correo electrónico a verificar.
     * @return true si existe un usuario con el correo electrónico especificado, de lo contrario false.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Encuentra un usuario por su correo electrónico.
     *
     * @param email el correo electrónico del usuario a buscar.
     * @return un {@link Optional} que contiene el usuario si se encuentra, de lo contrario vacío.
     */
    Optional<User> findByEmail(String email);

    /**
     * Encuentra las clases asociadas a un usuario específico por su ID.
     *
     * @param userId el ID del usuario cuyas clases se quieren encontrar.
     * @return una lista de clases asociadas al usuario proporcionado.
     */
    @Query("SELECT u.clases FROM User u WHERE u.id = :userId")
    List<Clase> findClasesByUserId(@Param("userId") Integer userId);

    /**
     * Encuentra los usuarios asociados a una clase específica por su ID.
     *
     * @param claseId el ID de la clase cuyos usuarios se quieren encontrar.
     * @return una lista de usuarios asociados a la clase proporcionada.
     */
    @Query("SELECT c.usuarios FROM Clase c WHERE c.idClase = :claseId")
    List<User> findUsersByClaseId(@Param("claseId") Long claseId);
}
