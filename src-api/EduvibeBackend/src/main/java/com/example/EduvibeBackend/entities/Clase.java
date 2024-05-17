package com.example.EduvibeBackend.entities;
import java.sql.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @ManyToOne
    @JoinColumn(name = "id_profesor")
    private Usuario profesor;


    @ManyToMany(mappedBy = "clasesInscritas")
    private List<Usuario> alumnos;

    @OneToMany(mappedBy = "clase")
    private List<Tarea> tareas;

  
}
