package com.example.EduvibeBackend.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.EduvibeBackend.utility.TokenUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que se ejecuta una vez por cada solicitud para autenticar las solicitudes HTTP basadas en el token de autorización.
 * Extiende OncePerRequestFilter para asegurar que se ejecute una vez por solicitud.
 */
@Component
public class RequestFilter extends OncePerRequestFilter {

    /**
     * Método que filtra y autentica las solicitudes HTTP entrantes.
     * 
     * @param request  la solicitud HTTP entrante.
     * @param response la respuesta HTTP saliente.
     * @param chain    el filtro de la cadena para pasar la solicitud y la respuesta al siguiente filtro.
     * @throws IOException      si ocurre un error de entrada/salida.
     * @throws ServletException si ocurre un error en el servlet.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader != null) {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
            try {
                // Obtiene la autenticación basada en el token de autorización
                usernamePasswordAuthenticationToken = TokenUtils.getAuthentication(requestTokenHeader);
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Establece la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } catch (Exception e) {
                // Registra el error si ocurre una excepción durante la autenticación
                logger.error(e.getMessage());
            }
        }

        // Pasa la solicitud y la respuesta al siguiente filtro en la cadena
        chain.doFilter(request, response);
    }
}
