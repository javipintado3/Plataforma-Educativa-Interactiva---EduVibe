package com.example.EduvibeBackend.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "clases")
public class Clase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClase;
    
    private String nombre;
    private String descripcion;

    @ManyToMany(mappedBy = "clases")
    private Set<User> usuarios = new HashSet<>();
    
    @OneToMany(mappedBy = "clase")
    private List<Tarea> tareas;
}


    



  

