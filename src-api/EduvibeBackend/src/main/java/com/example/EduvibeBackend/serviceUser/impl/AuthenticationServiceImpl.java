package com.example.EduvibeBackend.serviceUser.impl;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.request.SignUpRequest;
import com.example.EduvibeBackend.dto.request.SigninRequest;
import com.example.EduvibeBackend.dto.response.JwtAuthenticationResponse;
import com.example.EduvibeBackend.entities.Role;
import com.example.EduvibeBackend.entities.Usuario;
import com.example.EduvibeBackend.repository.UserRepository;
import com.example.EduvibeBackend.serviceUser.AuthenticationService;
import com.example.EduvibeBackend.serviceUser.JwtService;
import java.lang.IllegalArgumentException;

import lombok.Builder;

/**
 * Implementación del servicio de autenticación.
 */
@Builder
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructor para la inyección de dependencias.
     *
     * @param userRepository        Repositorio de usuarios.
     * @param passwordEncoder       Codificador de contraseñas.
     * @param jwtService            Servicio JWT.
     * @param authenticationManager Administrador de autenticación.
     */
    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    

    /**
     * Realiza la autenticación del usuario y devuelve un token JWT.
     *
     * @param request Datos del usuario para la autenticación.
     * @return Respuesta de autenticación que contiene el token JWT.
     */
    @Override
    public JwtAuthenticationResponse signin(SigninRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Usuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        String jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }



    /**
     * Registra un nuevo usuario y devuelve un token JWT.
     *
     * @param request Datos del usuario a registrar.
     * @return Respuesta de autenticación que contiene el token JWT.
     */
    @Override
    public JwtAuthenticationResponse signup(SignUpRequest request, Role role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        Usuario user = new Usuario();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(role); // Asignar el rol proporcionado
        userRepository.save(user);

        String jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }
}