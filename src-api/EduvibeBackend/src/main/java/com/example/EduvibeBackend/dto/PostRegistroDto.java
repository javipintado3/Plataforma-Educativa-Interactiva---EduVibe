package com.example.EduvibeBackend.dto;


import com.example.EduvibeBackend.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO (Data Transfer Object) utilizado para representar la información de registro de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostRegistroDto {
    /** Identificador del usuario. */
    Integer id;
    /** Correo electrónico del usuario. */
    String email;
    /** Nombre del usuario. */
    String nombre;
    /** Rol del usuario. */
    String rol;

    /**
     * Construye un objeto PostRegistroDto a partir de un objeto User.
     *
     * @param user El objeto User del que se obtendrá la información.
     * @return Un objeto PostRegistroDto con la información del usuario.
     */
    public static PostRegistroDto user(User user) {
        PostRegistroDto.PostRegistroDtoBuilder builder = PostRegistroDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nombre(user.getNombre())
                .rol(user.getRol());


        return builder.build();
    }
}

 