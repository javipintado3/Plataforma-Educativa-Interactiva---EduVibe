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

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (userOptional.isEmpty()) {
			throw new UsernameNotFoundException("El usuario no ha sido encontrado: " + email);
		}
		return userOptional.get();
	}

	public User registroUsuario(RegistroUserDto userDto) {
		boolean emailCorrecto = userDto.email().toLowerCase().contains("@eviden.com");
		String das = userDto.das();
		String expresion = "A\\d{6}";

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

}
