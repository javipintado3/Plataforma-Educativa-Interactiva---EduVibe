package com.example.EduvibeBackend.entities;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tarea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarea;

    private String enunciado;
    private Date fechaApertura;
    private Boolean estado;
    private Double calificacion;

    @ManyToOne
    @JoinColumn(name = "id_clase")
    private Clase clase;

    // Getters and Setters
}