package com.example.EduvibeBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsuarioDto {
	
	private Integer id;
	private String nombre;
	private String email;
	
	private String rol;
	
}
