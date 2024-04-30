package com.example.EduvibeBackend.controllerUser;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.EduvibeBackend.dto.response.UsuarioResponse;
import com.example.EduvibeBackend.entities.Usuario;

import lombok.RequiredArgsConstructor;

/**
 * Clase controladora para manejar operaciones relacionadas con la autorización y el acceso a recursos.
 * 
 * La ruta base para el controlador es "/api/v1/resources".
 */
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class AuthorizationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    /**
     * Punto de conexión GET para recuperar un saludo simple.
     * 
     * @return ResponseEntity con un mensaje de saludo.
     */
    @GetMapping
    public ResponseEntity<String> sayHello() {
        logger.info("## AuthorizationController :: sayHello");
        return ResponseEntity.ok("Aquí está tu recurso");
    }

    /**
     * Punto de conexión GET para recuperar la información del perfil de usuario.
     * 
     * @param usuario El usuario autenticado obtenido del contexto de seguridad.
     * @return ResponseEntity con la información del perfil de usuario.
     */
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal Usuario usuario) {
        logger.info("## AuthorizationController :: miPerfil");

        // Crear un objeto de respuesta con la información del perfil de usuario
        UsuarioResponse userResponse = new UsuarioResponse(
                usuario.getFirstName(),
                usuario.getLastName(),
                usuario.getEmail(),
                usuario.getRoles().toString()
        );

        return ResponseEntity.ok(userResponse);
    }
}