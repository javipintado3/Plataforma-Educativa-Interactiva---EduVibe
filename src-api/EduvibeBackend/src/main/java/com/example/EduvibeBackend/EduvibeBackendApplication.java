package com.example.EduvibeBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * La clase principal para la aplicación backend de EduVibe.
 * Esta clase contiene el método principal que ejecuta la aplicación Spring Boot.
 *
 * La configuración de CORS vive en {@link com.example.EduvibeBackend.security.SecurityConfig},
 * para que la apliquen también los filtros de seguridad.
 */
@SpringBootApplication
public class EduvibeBackendApplication {

    /**
     * El método principal que sirve como punto de entrada para la aplicación Spring Boot.
     * 
     * @param args argumentos de línea de comandos pasados a la aplicación
     */
    public static void main(String[] args) {
        SpringApplication.run(EduvibeBackendApplication.class, args);
    }
}
