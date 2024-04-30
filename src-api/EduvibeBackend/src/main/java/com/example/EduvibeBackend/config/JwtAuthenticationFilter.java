package com.example.EduvibeBackend.config;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.EduvibeBackend.serviceUser.JwtService;
import com.example.EduvibeBackend.serviceUser.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro de autenticación JWT que se ejecuta una vez por cada solicitud.
 * Este filtro verifica la presencia del token JWT en el encabezado de autorización,
 * lo valida y establece la autenticación en el contexto de seguridad de Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    /**
     * Método que realiza la lógica del filtro para verificar y autenticar el token JWT.
     *
     * @param request     Objeto HttpServletRequest que representa la solicitud HTTP.
     * @param response    Objeto HttpServletResponse que representa la respuesta HTTP.
     * @param filterChain Cadena de filtros para continuar el procesamiento de la solicitud.
     * @throws ServletException Excepción lanzada si hay un problema en el servlet.
     * @throws IOException      Excepción lanzada en caso de error de lectura/escritura.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Verificar si el encabezado de autorización está presente y comienza con "Bearer "
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            // Continuar con la cadena de filtros si no se proporciona un token JWT
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token JWT de la cadena del encabezado de autorización
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserName(jwt);

        // Verificar si el correo electrónico del usuario en el token no está vacío
        // y si no hay autenticación en el contexto de seguridad de Spring
        if (StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargar detalles del usuario desde el servicio de detalles del usuario
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

            // Verificar si el token JWT es válido para el usuario
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Configurar la autenticación en el contexto de seguridad de Spring
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}