package com.eduvibe.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.auth.AcceptInvitationRequest;
import com.eduvibe.dto.auth.AuthResponse;
import com.eduvibe.dto.auth.LoginRequest;
import com.eduvibe.dto.user.UserResponse;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.User;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;
import com.eduvibe.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Inicio de sesión y consulta de la sesión actual.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Mismo mensaje para email inexistente, contraseña incorrecta y cuenta no
     * activa. Diferenciarlos permitiría averiguar qué direcciones están dadas
     * de alta en la plataforma probando una a una.
     */
    private static final String CREDENCIALES_INVALIDAS = "Usuario y/o contraseña incorrectos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InvitationService invitationService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest peticion) {
        User usuario = userRepository.findByEmail(User.normalizarEmail(peticion.email()))
                .orElseThrow(() -> new BadCredentialsException(CREDENCIALES_INVALIDAS));

        // Una cuenta pendiente de aceptar invitación, o desactivada, no entra
        if (!usuario.puedeIniciarSesion()) {
            throw new BadCredentialsException(CREDENCIALES_INVALIDAS);
        }

        if (!passwordEncoder.matches(peticion.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException(CREDENCIALES_INVALIDAS);
        }

        return construirRespuesta(usuario);
    }

    /**
     * Activa la cuenta con la contraseña elegida y deja a la persona dentro, sin
     * obligarla a volver a escribir lo que acaba de teclear.
     */
    @Transactional
    public AuthResponse aceptarInvitacion(String token, AcceptInvitationRequest peticion) {
        User usuario = invitationService.aceptar(token, peticion.password());
        return construirRespuesta(usuario);
    }

    /** Usuario de la petición en curso, releído de base de datos. */
    @Transactional(readOnly = true)
    public UserResponse usuarioActual() {
        AuthenticatedUser autenticado = identidadActual();

        return userRepository.findById(autenticado.id())
                .map(UserResponse::de)
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));
    }

    /**
     * Identidad que el filtro ha dejado en el contexto de seguridad.
     *
     * @throws BadCredentialsException si no hay ninguna, lo que solo puede
     *         ocurrir si se llama desde una ruta pública por error.
     */
    public AuthenticatedUser identidadActual() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof AuthenticatedUser usuario)) {
            throw new BadCredentialsException("No hay ninguna sesión activa");
        }
        return usuario;
    }

    private AuthResponse construirRespuesta(User usuario) {
        return new AuthResponse(
                jwtService.emitirPara(usuario),
                jwtService.caducidadDeUnTokenNuevo(),
                UserResponse.de(usuario));
    }
}
