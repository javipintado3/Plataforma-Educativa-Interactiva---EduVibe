package com.example.EduvibeBackend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EduvibeBackend.dto.PostRegistroDto;
import com.example.EduvibeBackend.dto.UsuarioDto;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.service.impl.UserService;

/**
 * Controlador REST para gestionar los usuarios.
 */
@RestController
public class UserController {
    
    @Autowired
    UserService userService;
    
    /**
     * Obtener los usuarios de una clase específica.
     *
     * @param idClase el ID de la clase
     * @return una lista de usuarios de la clase envuelta en un ResponseEntity con estado HTTP 200 (OK)
     */
    @GetMapping("/{idClase}/usuarios")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosDeClase(@PathVariable Long idClase) {
        List<UsuarioDto> usuarios = userService.obtenerUsuariosDeClase(idClase);
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * Verificar si un correo electrónico ya existe en el sistema.
     *
     * @param email el correo electrónico a verificar
     * @return un ResponseEntity con el usuario si existe, o null si no existe
     */
    @GetMapping("/user/existeEmail")
    public ResponseEntity<?> existeEmail(@RequestParam Optional<String> email) {
        User user = null;
        ResponseEntity<?> result = null;
        try {
            user = userService.getUserByEmail(email.orElse(null));
        } catch (Exception e) {
            throw new GlobalException("Error al encontrar usuario");
        }
        
        if (user == null) {
            result = ResponseEntity.ok(null);
        } else {
            result = ResponseEntity.ok(PostRegistroDto.user(user));
        }
        return result;
    }
    
    /**
     * Obtener un usuario por su ID.
     *
     * @param id el ID del usuario
     * @return el usuario obtenido envuelto en un ResponseEntity con estado HTTP 200 (OK)
     */
    @GetMapping("user/{id}")
    public ResponseEntity<UsuarioDto> getUserById(@PathVariable Integer id) {
        UsuarioDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }
    
    /**
     * Cambiar la contraseña de un usuario.
     *
     * @param idUsuario el ID del usuario
     * @param newPassword la nueva contraseña
     * @return un ResponseEntity con un mensaje de éxito o error
     */
    @PutMapping("/user/changePassword/{idUsuario}")
    public ResponseEntity<?> changePassword(@PathVariable Integer idUsuario, @RequestBody String newPassword) {
        try {
            userService.changePassword(idUsuario, newPassword);
            return ResponseEntity.ok("Contraseña actualizada exitosamente");
        } catch (GlobalException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Listar todos los usuarios.
     *
     * @return una lista de todos los usuarios envuelta en un ResponseEntity con estado HTTP 200 (OK)
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDto>> listarTodosUsuarios() {
        List<UsuarioDto> usuarios = userService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * Obtener los usuarios de una clase específica.
     *
     * @param idClase el ID de la clase
     * @return una lista de usuarios de la clase
     */
    @GetMapping("usuarios/clase/{idClase}")
    public List<UsuarioDto> obtenerUsuariosPorClase(@PathVariable Long idClase) {
        try {
            return userService.obtenerUsuariosPorClase(idClase);
        } catch (GlobalException e) {
            // Manejo de excepciones
            // Por ejemplo, devolver un mensaje de error al cliente
            return null; // O un ResponseEntity con un mensaje de error
        }
    }
    
    /**
     * Editar un usuario existente.
     *
     * @param id el ID del usuario a editar
     * @param usuarioDto objeto DTO con los nuevos detalles del usuario
     * @return el usuario actualizado envuelto en un ResponseEntity con estado HTTP 200 (OK), o un mensaje de error si falla
     */
    @PutMapping("/user/editar/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Integer id, @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioDto updatedUser = userService.editarUsuario(id, usuarioDto);
            return ResponseEntity.ok(updatedUser);
        } catch (GlobalException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Eliminar un usuario por su ID.
     *
     * @param id el ID del usuario a eliminar
     * @return un ResponseEntity con un mensaje de éxito o error
     */
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
        try {
            userService.eliminarUsuario(id);
            return ResponseEntity.ok("Usuario eliminado exitosamente");
        } catch (GlobalException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
