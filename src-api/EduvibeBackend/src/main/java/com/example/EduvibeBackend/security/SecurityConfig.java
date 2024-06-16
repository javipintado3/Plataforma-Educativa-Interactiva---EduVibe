package com.example.EduvibeBackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.EduvibeBackend.service.impl.UserService;

/**
 * Configuración de seguridad para la aplicación.
 * Esta clase configura la autenticación y autorización para las solicitudes HTTP.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    AuthEntryPoint authEntryPoint;

    @Autowired
    RequestFilter requestFilter;

    /**
     * Método para obtener el servicio de detalles de usuario.
     *
     * @return una instancia de {@link UserService}.
     */
    @Bean
    UserService userDetailsService() {
        return new UserService();
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
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
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
     * Método para configurar la cadena de filtros de seguridad.
     * Aquí se especifica qué solicitudes están permitidas y cuáles requieren autenticación.
     *
     * @param http el objeto {@link HttpSecurity}.
     * @return una instancia de {@link SecurityFilterChain}.
     * @throws Exception en caso de que ocurra un error.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests((requests) -> {
                requests
                    .requestMatchers("/registeruser").permitAll()
                    .requestMatchers("/user/changePassword").authenticated()
                    .requestMatchers("/loginuser").permitAll()
                    .requestMatchers("/validate").permitAll()
                    .requestMatchers("/prueba").authenticated()
                    .requestMatchers("usuarios").hasAnyAuthority("admin", "profesor")
                    .requestMatchers("/clases/**").authenticated()
                    .requestMatchers("/clases/editar/{id}").hasAnyAuthority("admin", "profesor")
                    .requestMatchers("/clases/crear").hasAuthority("admin")
                    .requestMatchers("/clases/eliminar/{id}").hasAuthority("admin")
                    .requestMatchers("/clases/inscribir").hasAuthority("admin")
                    .requestMatchers("/tareas/crear").hasAnyAuthority("admin", "profesor")
                    .requestMatchers("/tareas/{id}").authenticated()
                    .requestMatchers("/tareas/todos").authenticated()
                    .requestMatchers("/tareas/eliminar/{id}").hasAnyAuthority("admin", "profesor")
                    .requestMatchers("/tareas/editar/{id}").hasAnyAuthority("admin", "profesor")
                    .requestMatchers("/tareas/clase/{idClase}").authenticated()
                    .anyRequest().permitAll();
            })
            .formLogin((form) -> form.permitAll())
            .logout((logout) -> logout.permitAll().logoutSuccessUrl("/"));

        http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
