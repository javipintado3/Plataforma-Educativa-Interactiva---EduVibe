package com.example.EduvibeBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.entities.Tarea;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
	
	


}
