package com.example.EduvibeBackend.dto.request;

/**
 * Clase que representa la solicitud de inicio de sesión (Signin).
 * Contiene los campos necesarios para autenticar a un usuario.
 */
public class SigninRequest {
	
	/**
    * Correo electrónico del usuario.
    */
   private String email;

   /**
    * Contraseña del usuario.
    */
   private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}