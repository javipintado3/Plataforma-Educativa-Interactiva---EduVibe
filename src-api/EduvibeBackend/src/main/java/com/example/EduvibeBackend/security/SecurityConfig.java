package com.example.EduvibeBackend.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.EduvibeBackend.service.impl.UserService;

/**
 * Configuración de seguridad para la aplicación.
 * Esta clase configura la autenticación y autorización para las solicitudes HTTP.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "admin";
    private static final String PROFESOR = "profesor";

    private final AuthEntryPoint authEntryPoint;
    private final RequestFilter requestFilter;

    /**
     * Orígenes autorizados para CORS, separados por comas, configurables con la
     * variable de entorno CORS_ALLOWED_ORIGINS para no tener que tocar el código
     * al desplegar.
     */
    private final String allowedOrigins;

    /**
     * Inyección por constructor: deja explícito de qué depende esta clase y
     * permite declarar los campos como final.
     */
    public SecurityConfig(AuthEntryPoint authEntryPoint, RequestFilter requestFilter,
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.authEntryPoint = authEntryPoint;
        this.requestFilter = requestFilter;
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Método que suministra la codificación de contraseñas.
     *
     * @return una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Método que autentifica los usuarios.
     *
     * @return una instancia de {@link DaoAuthenticationProvider}.
     */
    @Bean
    DaoAuthenticationProvider authenticationProvider(UserService userService,
            BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Método que proporciona el {@link AuthenticationManager}.
     *
     * @param authConfig la configuración de autenticación.
     * @return una instancia de {@link AuthenticationManager}.
     * @throws Exception en caso de que ocurra un error.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configuración de CORS. Se registra aquí, y no como WebMvcConfigurer, para que
     * la cadena de filtros de Spring Security la aplique también a las peticiones
     * preflight (OPTIONS) de los endpoints protegidos.
     *
     * @return el origen de configuración CORS.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        List<String> origenes = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Método para configurar la cadena de filtros de seguridad.
     *
     * Las reglas se evalúan en orden y gana la primera que coincide, así que las
     * rutas concretas van siempre antes que los comodines. La regla final es
     * {@code authenticated()}: todo lo que no esté declarado como público exige
     * sesión, para que añadir un endpoint nuevo no lo deje abierto por descuido.
     *
     * @param http el objeto {@link HttpSecurity}.
     * @return una instancia de {@link SecurityFilterChain}.
     * @throws Exception en caso de que ocurra un error.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(requests -> requests
                // Spring reenvía los errores a /error. Si esa ruta exige autenticación,
                // un 403 acaba devolviéndose al cliente como un 401 engañoso.
                .requestMatchers("/error").permitAll()

                // --- Público: alta de cuenta y autenticación ---
                .requestMatchers(HttpMethod.POST, "/loginuser", "/registeruser").permitAll()
                .requestMatchers(HttpMethod.GET, "/validate", "/user/existeEmail").permitAll()

                // --- Clases ---
                .requestMatchers(HttpMethod.POST, "/clases/crear", "/clases/inscribir").hasAuthority(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/clases/eliminar/**", "/clases/*/eliminar-usuario").hasAuthority(ADMIN)
                .requestMatchers(HttpMethod.PUT, "/clases/editar/**").hasAnyAuthority(ADMIN, PROFESOR)
                .requestMatchers("/clases/**").authenticated()

                // --- Tareas ---
                .requestMatchers(HttpMethod.POST, "/tareas/crear", "/tareas/crear/**", "/tareas/asignar")
                    .hasAnyAuthority(ADMIN, PROFESOR)
                .requestMatchers(HttpMethod.PUT, "/tareas/editar/**", "/tareas/calificacion/**")
                    .hasAnyAuthority(ADMIN, PROFESOR)
                .requestMatchers(HttpMethod.DELETE, "/tareas/eliminar/**").hasAnyAuthority(ADMIN, PROFESOR)
                .requestMatchers(HttpMethod.GET, "/tareas/mediaCalificaciones/**").hasAnyAuthority(ADMIN, PROFESOR)
                .requestMatchers("/tareas/**").authenticated()

                // --- Usuarios ---
                .requestMatchers(HttpMethod.DELETE, "/user/**").hasAuthority(ADMIN)
                .requestMatchers(HttpMethod.GET, "/usuarios").hasAnyAuthority(ADMIN, PROFESOR)

                // --- Todo lo demás exige autenticación ---
                .anyRequest().authenticated());

        http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
