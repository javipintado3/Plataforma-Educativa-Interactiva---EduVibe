package com.example.EduvibeBackend.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
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

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;
	private String password;
	private String nombre;
	private String apellido;
	

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "usuario")
	@JsonIgnore
	private Set<UsuarioRol> usuarioRoles = new HashSet<>();
	
    // Relación con Clase para representar al profesor
    @OneToOne(mappedBy = "profesor")
    private Clase claseProfesor;

    // Relación con Clase para representar a los alumnos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id") // nombre de la columna en la tabla de Usuario que referencia a la Clase
    private Clase clase;
	


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public Set<UsuarioRol> getUsuarioRoles() {
		return usuarioRoles;
	}

	public void setUsuarioRoles(Set<UsuarioRol> usuarioRoles) {
		this.usuarioRoles = usuarioRoles;
	}

	public Usuario() {
		
	}
	

    

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		Set<Authority> autoridades = new HashSet<>();
		this.usuarioRoles.forEach(usuarioRol -> {
			autoridades.add(new Authority(usuarioRol.getRol().getNombre()));
		});
		return autoridades;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

}
