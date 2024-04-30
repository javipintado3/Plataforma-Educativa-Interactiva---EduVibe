package com.example.EduvibeBackend.controllerUser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.EduvibeBackend.dto.response.UsuarioResponse;
import com.example.EduvibeBackend.serviceUser.UserService;




/**
 * Controlador que maneja las operaciones relacionadas con la autorización de administradores.
 * Proporciona endpoints para obtener información sobre los usuarios con el rol de administrador.
 */
@RestController
@RequestMapping("/api/v1/users")
public class AuthorizationAdminController {
    
	// Definición de un logger para la clase AuthorizationAdminController
	// Se utiliza para registrar eventos y mensajes durante la ejecución de la aplicación.
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAdminController.class);

    @Autowired
    private UserService userService;

    /**
     * Obtiene la lista de usuarios con el rol de administrador.
     *
     * @return ResponseEntity con la lista de respuestas UsuarioResponse y el código de estado correspondiente.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> showUsers() {
        logger.info("## AuthorizationAdminController :: showUsers");
        List<UsuarioResponse> userList = userService.getAllUsers();
        return ResponseEntity.ok(userList);
    }
}