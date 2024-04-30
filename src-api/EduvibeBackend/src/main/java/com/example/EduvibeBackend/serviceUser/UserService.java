package com.example.EduvibeBackend.serviceUser;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.EduvibeBackend.dto.response.UsuarioResponse;




/**
 * Interfaz que define los métodos de servicio para operaciones relacionadas con la entidad Usuario.
 */
public interface UserService {

    /**
     * Obtiene el servicio de detalles de usuario.
     *
     * @return Servicio de detalles de usuario.
     */
    UserDetailsService userDetailsService();

    /**
     * Obtiene una lista de respuestas de usuario que contienen información básica de todos los usuarios.
     *
     * @return Lista de respuestas de usuario.
     */
    List<UsuarioResponse> getAllUsers();
}