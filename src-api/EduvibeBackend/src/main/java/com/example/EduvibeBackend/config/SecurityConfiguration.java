package com.example.EduvibeBackend.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.EduvibeBackend.entities.Role;
import com.example.EduvibeBackend.serviceUser.UserService;




/**
 * Configuración de seguridad para la aplicación. Define la configuración para la autenticación y la autorización
 * mediante el uso de JSON Web Tokens (JWT).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserService userService;

    /**
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(request ->
                request
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/prendas/**")
                        .hasAnyAuthority(Role.ROLE_USER.toString(), Role.ROLE_ADMIN.toString())
                    .requestMatchers(HttpMethod.POST, "/api/v1/prendas/**")
                        .hasAuthority(Role.ROLE_ADMIN.toString())
                    .requestMatchers(HttpMethod.PUT, "/api/v1/prendas/**")
                        .hasAuthority(Role.ROLE_ADMIN.toString())
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/prendas/**")
                        .hasAuthority(Role.ROLE_ADMIN.toString())
                    .requestMatchers("/api/v1/users/**")
                        .hasAuthority("ROLE_ADMIN") 
                    .anyRequest().authenticated())
            .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
            .cors(Customizer.withDefaults()) // Configurar CORS aquí con Customizer
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    */

    /**
     * Configura el codificador de contraseñas para la aplicación.
     *
     * @return Instancia de PasswordEncoder configurada.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación para la aplicación.
     *
     * @return Instancia de AuthenticationProvider configurada.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configura el administrador de autenticación para la aplicación.
     *
     * @param config Objeto AuthenticationConfiguration que contiene la configuración de autenticación.
     * @return Instancia de AuthenticationManager configurada.
     * @throws Exception Excepción lanzada si hay un problema con la configuración de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura la fuente de configuración CORS para permitir solicitudes desde cualquier origen.
     *
     * @return Fuente de configuración CORS configurada.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}