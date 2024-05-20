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


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	AuthEntryPoint authEntryPoint;
	
	@Autowired
	RequestFilter requestFilter;
	
	@Bean
	UserService userDetailsService() {
		return new UserService();
	}
  
	// Método que nos suministrará la codificación
	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	// Método que autentifica
	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	 @Bean
	 AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
	    return authConfig.getAuthenticationManager();
	  }
	
	
	// Aquí es donde podemos especificar qué es lo que hace y lo que no
	// según el rol del usuario
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authEntryPoint))
	        .authorizeHttpRequests((requests) -> {
			requests
				.requestMatchers("/registeruser").permitAll()
				.requestMatchers("/loginuser").permitAll()
				.requestMatchers("/validate").permitAll()
				.requestMatchers("/prueba").authenticated()
				.requestMatchers("/reserva/**").authenticated()
				.requestMatchers("/puesto/editar/{id}").hasAuthority("admin")
				.requestMatchers("/puesto/todos").hasAuthority("admin")
				.requestMatchers("/puesto/{id}").hasAuthority("admin")
				.requestMatchers("/proyecto/crear").hasAuthority("admin")
				.requestMatchers("/proyecto/todos").authenticated()
				.requestMatchers("/proyecto/{nombre}").authenticated()
				.requestMatchers("/proyecto/eliminar/{nombre}").hasAuthority("admin")
				.requestMatchers("/proyecto/editar/{nombre}").hasAuthority("admin")
				.anyRequest().permitAll();
	        })
	        .formLogin((form) -> form.permitAll())
	        .logout((logout) -> logout.permitAll().logoutSuccessUrl("/"));
		
       http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}


}
