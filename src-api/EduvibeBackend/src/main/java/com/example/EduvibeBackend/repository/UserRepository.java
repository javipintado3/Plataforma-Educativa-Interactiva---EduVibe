package com.example.EduvibeBackend.repository;




import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EduvibeBackend.entities.User;

@Repository
//Creamos un repo en el cual le pasamos un usuario y podemos buscar dicho usuario por su email en la BBDD
public interface UserRepository extends JpaRepository<User, Integer> {


    boolean existsByEmailIgnoreCase(String email);
    //User findByUsername(String username);


    Optional<User> findByEmail(String email);
  
   


}
