package com.example.EduvibeBackend.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.transaction.Transactional;

/**
 * Clase de entidad que representa un usuario. Anotada con JPA para mapearla a
 * la base de datos. Implementa UserDetails para la autenticación de Spring
 * Security.
 */
@Entity
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 1L;

	/**
	 * Id del usuario.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Nombre del usuario.
	 */
	private String firstName;

	/**
	 * Apellido del usuario.
	 */
	private String lastName;

	/**
	 * Correo electrónico único del usuario.
	 */
	@Column(unique = true)
	private String email;

	/**
	 * Contraseña del usuario.
	 */
	private String password;

	/**
	 * Roles asignados al usuario.
	 */
	@ElementCollection(fetch = FetchType.EAGER, targetClass = Role.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "usuario_rol")
	@Column(name = "RolesUsuario")
	private Set<Role> roles = new HashSet<>();
	
	


	/**
	 * Obtiene la colección de roles asignados al usuario. Este método es anotado
	 * con @Transactional para cargar la colección de roles de manera temprana.
	 *
	 * @return Colección de roles del usuario.
	 */
	@Transactional
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Cargar la colección de roles de manera temprana
		roles.size();

		return roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
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

	@Override
	public String getPassword() {
		return password;
	}

	// Métodos setter añadidos
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

}