package com.example.EduvibeBackend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

@RestController
public class UserController {
	
	@Autowired
	UserService userService;
	
	@GetMapping("/user/existeEmail")
	public ResponseEntity<?> existeEmail(@RequestParam Optional<String> email){
		User user = null;
		ResponseEntity result = null;
		try {
			user = userService.getUserByEmail(email.orElse(null));
		} catch (Exception e) {
			throw new GlobalException("Error al encontrar usuario");
		}
		
		if(user==null) {
			result = ResponseEntity.ok(null);
		}else {
			result = ResponseEntity.ok(PostRegistroDto.user(user));
		}
		return result;
	}
	
    @GetMapping("user/{id}")
    public ResponseEntity<UsuarioDto> getUserById(@PathVariable Integer id) {
        UsuarioDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }
	
    @PutMapping("/user/changePassword/{idUsuario}")
    public ResponseEntity<?> changePassword(@PathVariable Integer idUsuario , @RequestBody
    		String newPassword) {
        try {
            userService.changePassword(idUsuario, newPassword);
            return ResponseEntity.ok("Contraseña actualizada exitosamente");
        } catch (GlobalException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDto>> listarTodosUsuarios() {
        List<UsuarioDto> usuarios = userService.listarTodosUsuarios(); // Obtener la lista de todos los usuarios
        return ResponseEntity.ok(usuarios); // Devolver la lista de usuarios en la respuesta
    }
    
    
    
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
    
    @PutMapping("/user/editar/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Integer id, @RequestBody UsuarioDto usuarioDto) {
        try {
            UsuarioDto updatedUser = userService.editarUsuario(id, usuarioDto);
            return ResponseEntity.ok(updatedUser);
        } catch (GlobalException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

	
}
