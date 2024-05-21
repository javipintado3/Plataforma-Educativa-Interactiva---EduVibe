package com.example.EduvibeBackend.dto;



public record RegistroUserDto(
                              String email,
                              String nombre,
                              String password,
                              String rol) {
}
