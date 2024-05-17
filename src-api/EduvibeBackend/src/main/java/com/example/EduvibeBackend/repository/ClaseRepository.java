package com.example.EduvibeBackend.repository;

import com.example.EduvibeBackend.entities.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
}
