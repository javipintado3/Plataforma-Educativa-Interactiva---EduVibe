package com.example.EduvibeBackend.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.RegistroUserDto;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder encode;

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}
	
	public User getUserById(Integer idUser) {
		return userRepository.findById(idUser).orElse(null);
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
	
	public void changePassword(String email, String newPassword) {
        // Verificar que la nueva contraseña cumpla con los requisitos de seguridad
        if (newPassword.length() < 8) {
            throw new GlobalException("La contraseña debe tener al menos 8 caracteres");
        }

        // Buscar el usuario por su correo electrónico
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new GlobalException("Usuario no encontrado con el correo electrónico proporcionado"));

        // Codificar la nueva contraseña
        String newPasswordEncoded = encode.encode(newPassword);

        // Establecer la nueva contraseña para el usuario
        user.setPassword(newPasswordEncoded);

        // Guardar los cambios en el repositorio
        userRepository.save(user);
    }

}
