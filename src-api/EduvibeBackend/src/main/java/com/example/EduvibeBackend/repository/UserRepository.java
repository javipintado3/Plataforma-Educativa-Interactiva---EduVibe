package com.example.EduvibeBackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EduvibeBackend.entities.Usuario;



/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Usuario.
 */
@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {

    /**
     * Encuentra un usuario por su dirección de correo electrónico.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @return Usuario que corresponde a la dirección de correo electrónico especificada.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Encuentra un usuario por su identificación.
     *
     * @param id Identificación del usuario.
     * @return Usuario que corresponde a la identificación especificada.
     */
    Optional<Usuario> findById(Long id);

    /**
     * Verifica si existe un usuario con la dirección de correo electrónico especificada.
     *
     * @param email Dirección de correo electrónico a verificar.
     * @return true si existe un usuario con la dirección de correo electrónico especificada, false de lo contrario.
     */
    Boolean existsByEmail(String email);
}