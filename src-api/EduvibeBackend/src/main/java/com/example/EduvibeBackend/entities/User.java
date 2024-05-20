package com.example.EduvibeBackend.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // Crear constructor con todos los argumentos
@NoArgsConstructor // Crear constructor sin argumentos
@Data // Crear getter y setter
@Entity // Declara que es una tabla de la bbdd
@Table(name = "usuarios")
public class User implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id; // Campo id autogenerado

	@NotNull
    private String nombre; // Nombre del usuario
 
 
    @Email(message = "El email no es válido")
    @NotNull
 
    private String email; // Email del usuario
 
    // @Size(min = 7, max = 50, message = "La contraseña tiene que contener mínimo 8
    // carácteres y 50 de máximo")
    @NotNull
    private String password; // Contraseña del usuario
 
    private String rol;

	// Relación con Clase para representar al profesor
	@OneToOne(mappedBy = "profesor")
	private Clase claseProfesor;

	// Relación con Clase para representar a los alumnos
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clase_id") // nombre de la columna en la tabla de Usuario que referencia a la Clase
	private Clase clase;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(rol));
        return authorities;
    }
 
    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return password;
    }
 
    @Override
    public String getUsername() {
 
        return email;
    }
 
    @Override
    public boolean isAccountNonExpired() {
 
        return true;
    }
 
    @Override
    public boolean isAccountNonLocked() {
        return true;
 
    }
 
    @Override
    public boolean isCredentialsNonExpired() {
 
        return true;
    }
 
    @Override
    public boolean isEnabled() {
 
        return true;
    }

}
