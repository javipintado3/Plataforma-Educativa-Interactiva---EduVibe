package com.example.EduvibeBackend.entities;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Clase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClase;

    private String nombre;
    private String descripcion;


    @OneToMany(mappedBy = "clase")
    private List<Tarea> tareas;
    

    @OneToOne
    @JoinColumn(name = "profesor_id", referencedColumnName = "id")
    private User profesor;

    @OneToMany(mappedBy = "clase")
    private List<User> alumnos;

  
}
