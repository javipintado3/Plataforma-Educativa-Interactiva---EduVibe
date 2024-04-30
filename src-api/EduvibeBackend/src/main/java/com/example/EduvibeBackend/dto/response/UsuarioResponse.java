package com.example.EduvibeBackend.dto.response;

/**
 * Clase que representa la estructura de respuesta para la información de un
 * usuario. Contiene el nombre, apellido, correo electrónico y el rol del
 * usuario.
 */
public class UsuarioResponse {
	/**
	 * Nombre del usuario.
	 */
	private String firstName;

	/**
	 * Apellido del usuario.
	 */
	private String lastName;

	/**
	 * Correo electrónico del usuario.
	 */
	private String email;

	/**
	 * Rol del usuario.
	 */
	private String rol;

	/**
	 * Constructor que inicializa una instancia de UsuarioResponse con la
	 * información proporcionada.
	 *
	 * @param firstName Nombre del usuario.
	 * @param lastName  Apellido del usuario.
	 * @param email     Correo electrónico del usuario.
	 * @param rol       Rol del usuario.
	 */
	public UsuarioResponse(String firstName, String lastName, String email, String rol) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.rol = rol;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

}