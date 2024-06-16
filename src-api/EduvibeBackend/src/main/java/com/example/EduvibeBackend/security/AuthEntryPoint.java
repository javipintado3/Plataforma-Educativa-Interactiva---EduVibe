package com.example.EduvibeBackend.security;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Clase que maneja los intentos de acceso no autorizado a recursos protegidos.
 * 
 * HTTP en caso de fallos de autenticación.
 */
@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * Método que se invoca cuando un usuario no autenticado intenta acceder a un
     * recurso protegido.
     *
     * @param request       el objeto {@link HttpServletRequest} que contiene la
     *                      solicitud del cliente.
     * @param response      el objeto {@link HttpServletResponse} que contiene la
     *                      respuesta enviada al cliente.
     * @param authException la excepción lanzada cuando falla la autenticación.
     * @throws IOException          si ocurre un error de entrada/salida.
     * @throws ServletException     si ocurre un error relacionado con el servlet.
     * @throws StreamWriteException si ocurre un error al escribir el flujo de
     *                              datos.
     * @throws DatabindException    si ocurre un error al enlazar los datos.
     * @throws java.io.IOException  si ocurre un error de entrada/salida.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException)
            throws IOException, ServletException, StreamWriteException, DatabindException, java.io.IOException {

        response.addHeader("Content-Type", "application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
