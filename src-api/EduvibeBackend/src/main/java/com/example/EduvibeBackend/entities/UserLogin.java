package com.example.EduvibeBackend.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {

    //Creamos una entidad con los datos que vamos a recibir en la BBDD
    String email;
    String password;
    
}
