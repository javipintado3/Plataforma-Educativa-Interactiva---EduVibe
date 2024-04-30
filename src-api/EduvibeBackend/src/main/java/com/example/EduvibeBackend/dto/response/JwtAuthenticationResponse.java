package com.example.EduvibeBackend.dto.response;

/**
 * Clase que representa la estructura de respuesta para autenticación mediante JWT (Json Web Token).
 * Contiene el token generado para el usuario autenticado.
 */
public class JwtAuthenticationResponse {

    /**
     * Token de autenticación JWT.
     */
    private String token;

    /**
     * Constructor que inicializa una instancia de JwtAuthenticationResponse con el token proporcionado.
     *
     * @param token Token de autenticación JWT.
     */
    public JwtAuthenticationResponse(String token) {
        this.token = token;
    }

    /**
     * Obtiene el token de autenticación JWT.
     *
     * @return Token de autenticación JWT.
     */
    public String getToken() {
        return token;
    }

    /**
     * Establece el token de autenticación JWT.
     *
     * @param token Token de autenticación JWT.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Método estático para obtener un constructor de JwtAuthenticationResponse.
     *
     * @return Constructor de JwtAuthenticationResponse.
     */
    public static JwtAuthenticationResponseBuilder builder() {
        return new JwtAuthenticationResponseBuilder();
    }

    /**
     * Clase interna que actúa como constructor para JwtAuthenticationResponse
     * permitiendo la construcción de instancias con un estilo fluido.
     */
    public static class JwtAuthenticationResponseBuilder {

        /**
         * Token de autenticación JWT.
         */
        private String token;

        /**
         * Establece el token de autenticación JWT.
         *
         * @param token Token de autenticación JWT.
         * @return El constructor para continuar con la construcción.
         */
        public JwtAuthenticationResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Construye y devuelve una instancia de JwtAuthenticationResponse con los valores establecidos.
         *
         * @return Instancia de JwtAuthenticationResponse.
         */
        public JwtAuthenticationResponse build() {
            return new JwtAuthenticationResponse(token);
        }
    }
}