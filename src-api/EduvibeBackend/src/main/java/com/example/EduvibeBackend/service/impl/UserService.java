package com.example.EduvibeBackend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.RegistroUserDto;
import com.example.EduvibeBackend.dto.UsuarioDto;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.ClaseRepository;
import com.example.EduvibeBackend.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder encode;
	
	 @Autowired
	    private ClaseRepository claseRepository;

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}
	
    public UsuarioDto getUserById(Integer idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new GlobalException("Usuario no encontrado con el ID proporcionado: " + idUser));
        return convertirAUsuarioDto(user);
    }
    

    public List<UsuarioDto> obtenerUsuariosDeClase(Long idClase) {
        Clase clase = claseRepository.findById(idClase)
                .orElseThrow(() -> new GlobalException("Clase no encontrada"));
        
        List<User> users = userRepository.findUsersByClaseId(idClase);
        
        return users.stream().map(this::convertirAUsuarioDto).collect(Collectors.toList());
        
        
    }

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (userOptional.isEmpty()) {
			throw new UsernameNotFoundException("El usuario no ha sido encontrado: " + email);
		}
		return userOptional.get();
	}

	public User registroUsuario(RegistroUserDto userDto) {
	
		
		// Verificar que el email termine en "@vibe.com"
        if (!userDto.email().toLowerCase().endsWith("@vibe.com")) {
            throw new GlobalException("El email debe terminar en '@vibe.com'");
        }
	

		if (userRepository.existsByEmailIgnoreCase(userDto.email())) {
			throw new GlobalException("El email ya ha sido registrado");
		}
		if (userDto.password().length() < 8) {
			throw new GlobalException("El campo contraseña debe de contener 8 o más caracteres");
		}

		User user = new User();
		user.setEmail(userDto.email());
		user.setNombre(userDto.nombre());
		user.setPassword(encode.encode(userDto.password()));
		user.setRol((userDto.rol() != null) ? userDto.rol() : "alumno"); // Asignar "user" si el rol es null

		User save = userRepository.save(user);
		return save;
	}
	
	public void changePassword(Integer idUsuario, String newPassword) {
		
        // Buscar el usuario por su ID
        User UserLogin = userRepository.findById(idUsuario)
                .orElseThrow(() -> new GlobalException("Usuario no encontrado"));
        
        // Verificar que la nueva contraseña cumpla con los requisitos de seguridad
        if (newPassword.length() < 8) {
        	throw new GlobalException("El campo contraseña debe de contener 8 o más caracteres");
        }

        // Codificar la nueva contraseña
        String newPasswordEncoded = encode.encode(newPassword);

        // Establecer la nueva contraseña para el usuario
        UserLogin.setPassword(newPasswordEncoded);

        // Guardar los cambios en el repositorio
        userRepository.save(UserLogin);
    }
	
	   // Método para obtener todos los usuarios y convertirlos a objetos UsuarioDto
    public List<UsuarioDto> listarTodosUsuarios() {
        List<User> usuarios = userRepository.findAll(); // Consulta todos los usuarios en la base de datos
        // Convierte la lista de usuarios a una lista de UsuarioDto
        List<UsuarioDto> usuariosDto = usuarios.stream()
                .map(this::convertirAUsuarioDto) // Utiliza el método convertirAUsuarioDto para convertir cada usuario
                .collect(Collectors.toList()); // Recolecta los resultados en una lista
        return usuariosDto; // Devuelve la lista de UsuarioDto
    }

    // Método para convertir un objeto User a UsuarioDto
    private UsuarioDto convertirAUsuarioDto(User usuario) {
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setId(usuario.getId());
        usuarioDto.setNombre(usuario.getNombre());
        usuarioDto.setEmail(usuario.getEmail());
        usuarioDto.setRol(usuario.getRol());
        return usuarioDto;
    }
    
    public List<UsuarioDto> obtenerUsuariosPorClase(Long idClase) {
        List<User> usuarios = userRepository.findUsersByClaseId(idClase);

        if (usuarios.isEmpty()) {
            throw new GlobalException("No hay usuarios en esta clase");
        }

        return usuarios.stream()
                .map(this::convertirAUsuarioDto)
                .collect(Collectors.toList());
    }
    
    // Nuevo método para editar usuarios
    public UsuarioDto editarUsuario(Integer idUser, UsuarioDto usuarioDto) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new GlobalException("Usuario no encontrado con el ID proporcionado: " + idUser));

        if (usuarioDto.getNombre() != null) {
            user.setNombre(usuarioDto.getNombre());
        }
        if (usuarioDto.getEmail() != null) {
            if (!user.getEmail().equals(usuarioDto.getEmail()) && userRepository.existsByEmailIgnoreCase(usuarioDto.getEmail())) {
                throw new GlobalException("El email ya ha sido registrado");
            }
            if (!usuarioDto.getEmail().toLowerCase().endsWith("@vibe.com")) {
                throw new GlobalException("El email debe terminar en '@vibe.com'");
            }
            user.setEmail(usuarioDto.getEmail());
        }
        if (usuarioDto.getRol() != null) {
            user.setRol(usuarioDto.getRol());
        }
        
        User updatedUser = userRepository.save(user);
        return convertirAUsuarioDto(updatedUser);
    }

}
