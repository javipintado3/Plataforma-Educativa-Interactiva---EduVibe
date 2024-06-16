package com.example.EduvibeBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * La clase principal para la aplicación backend de EduVibe.
 * Esta clase contiene el método principal que ejecuta la aplicación Spring Boot.
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

    /**
     * Configura los mapeos de CORS para la aplicación.
     * 
     * @return un configurador de WebMvc
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200", "http://localhost")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                    .allowCredentials(true)
                    .allowedHeaders("*");
            }
        };
    }
}
