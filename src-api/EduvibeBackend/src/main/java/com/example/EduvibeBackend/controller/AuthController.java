package com.example.EduvibeBackend.controller;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.EduvibeBackend.dto.PostRegistroDto;
import com.example.EduvibeBackend.dto.RegistroUserDto;
import com.example.EduvibeBackend.dto.TokenDTO;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.entities.UserLogin;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.service.impl.UserService;
import com.example.EduvibeBackend.utility.TokenUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

@RestController
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserService userService;

	/**
	 * Metodo para validar que el token no esta malformado, no ha expirado o no
	 * existe recogido mediante la cabecera "Authorization". A traves del
	 * subjet(email) se busca al usuario, si no existe o está eliminado logico pues
	 * no es valido.
	 * 
	 * @param token cadena del token
	 * @return usuario con sus atributos a excepcion de la contraseña
	 * @throws JwtException
	 * @throws IllegalArgumentException
	 * @throws NoSuchAlgorithmException
	 */
	@GetMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token)
			throws JwtException, IllegalArgumentException, NoSuchAlgorithmException {
		
		String emailToken = "";
		PostRegistroDto user = null;
		try {
			emailToken = TokenUtils.getAllClaimsFromToken(token.substring(7)).getSubject();
			System.out.println(TokenUtils.getAllClaimsFromToken(token.substring(7)).getExpiration());
		} catch (IllegalArgumentException e) {
			throw new GlobalException("Imposible encontrar un JWT Token");
		} catch (ExpiredJwtException e) {
			throw new GlobalException("Token expirado");
		} catch (NoSuchAlgorithmException e) {
			throw new GlobalException("Algoritmo no válido");
		} catch (MalformedJwtException e) {
			throw new GlobalException("Token malformado");
		}

		if (!emailToken.isBlank()) {
			try {
				user = PostRegistroDto.user(userService.getUserByEmail(emailToken));
				String role = (String) TokenUtils.getAllClaimsFromToken(token.substring(7)).get("role");
				if(!user.getRol().equals(role)) {
					throw new GlobalException("El rol del usuario ha cambiado");
				}
			} catch (Exception e) {
				// Falta contemplar que el usuario este en borrado logico.
				throw new GlobalException(e.getMessage());
			}
		}

		return ResponseEntity.ok(user);
	}
	
	@GetMapping("/renew")
	public ResponseEntity<?> renew(@RequestHeader("Authorization") String token)
			throws JwtException, IllegalArgumentException, NoSuchAlgorithmException {
		ResponseEntity<?> result = null;
		String emailToken = "";
		PostRegistroDto user = null;
		try {
			emailToken = TokenUtils.getAllClaimsFromToken(token.substring(7)).getSubject();
			System.out.println(TokenUtils.getAllClaimsFromToken(token.substring(7)).getExpiration());
		} catch (IllegalArgumentException e) {
			throw new GlobalException("Imposible encontrar un JWT Token");
		} catch (ExpiredJwtException e) {
			throw new GlobalException("Token expirado");
		} catch (NoSuchAlgorithmException e) {
			throw new GlobalException("Algoritmo no válido");
		} catch (MalformedJwtException e) {
			throw new GlobalException("Token malformado");
		}

		if (!emailToken.isBlank()) {
			try {
				user = PostRegistroDto.user(userService.getUserByEmail(emailToken));
				String role = (String) TokenUtils.getAllClaimsFromToken(token.substring(7)).get("role");
				if(!user.getRol().equals(role)) {
					throw new GlobalException("El rol del usuario ha cambiado");
				}
				
					// Generar un token JWT utilizando los detalles del usuario autenticado
					String jwt = TokenUtils.generateToken(user.getEmail(), user.getNombre(), user.getRol());

					// Devolver el token JWT en un ResponseEntity OK
					TokenDTO tokennew = new TokenDTO();
					tokennew.setToken(jwt);
					result = ResponseEntity.ok(tokennew);
				
			} catch (Exception e) {
				// Falta contemplar que el usuario este en borrado logico.
				throw new GlobalException(e.getMessage());
			}
		}

		return result;
	}

	

	// endpoint para registrar un usuario
	// lanza una excepcion si el email es repito y si no termina por @eviden.com
	@PostMapping("/registeruser")
	public ResponseEntity<?> registroUser(@RequestBody RegistroUserDto registroUserDto) {
		try {

			User user = userService.registroUsuario(registroUserDto);

			return ResponseEntity.status(HttpStatus.CREATED).body(PostRegistroDto.user(user));
		} catch (ResponseStatusException e) {
			throw new GlobalException(e.getMessage());
		}
	}

	/**
	 * Este método se utiliza para autenticar a un usuario.
	 * 
	 * @param loginRequest Los detalles de inicio de sesión del usuario (nombre de
	 *                     usuario y contraseña).
	 * @return ResponseEntity con el token JWT generado si la autenticación fue
	 *         exitosa.
	 */
	@PostMapping("/loginuser")
	public ResponseEntity<?> authenticateUser(@RequestBody UserLogin loginRequest) {

		Authentication authentication;

		try {
			// Autenticar al usuario utilizando el AuthenticationManager y el proveedor de
			// autenticación
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

			// Obtener los detalles del usuario autenticado
			UserDetails userDetails = userService.loadUserByUsername(loginRequest.getEmail());

			User info = userService.getUserByEmail(loginRequest.getEmail());

			// Generar un token JWT utilizando los detalles del usuario autenticado
			String jwt = TokenUtils.generateToken(userDetails.getUsername(), info.getNombre(), info.getRol());

			// Devolver el token JWT en un ResponseEntity OK

			TokenDTO token = new TokenDTO();
			token.setToken(jwt);
			return ResponseEntity.ok(token);

		} catch (Exception e) {
			// Si ocurre un error durante la autenticación, lanzamos una excepción
			// GlobalException
			throw new GlobalException(e.getMessage());
		}
	}

}
