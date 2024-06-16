package com.example.EduvibeBackend.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa una Tarea en el sistema.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tarea {

    /**
     * Identificador único de la tarea.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarea;

    /**
     * Nombre de la tarea.
     */
    @NotNull
    private String nombreTarea;

    /**
     * Enunciado o descripción de la tarea.
     */
    @NotNull
    private String enunciado;

    /**
     * Fecha de apertura de la tarea.
     */
    private LocalDate fechaApertura;

    /**
     * Estado de la tarea (por ejemplo, calificada o pendiente).
     */
    private Boolean estado;

    /**
     * Calificación de la tarea.
     */
    private Double calificacion;

    /**
     * Usuario al que se asigna la tarea.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /**
     * Solución escrita para la tarea.
     */
    private String solucionEscrita;

    /**
     * Clase a la que pertenece la tarea.
     */
    @ManyToOne
    @JoinColumn(name = "id_clase")
    private Clase clase;

    /**
     * Método que se ejecuta antes de persistir la entidad, para asignar la fecha de apertura.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaApertura = LocalDate.now();
    }
}
