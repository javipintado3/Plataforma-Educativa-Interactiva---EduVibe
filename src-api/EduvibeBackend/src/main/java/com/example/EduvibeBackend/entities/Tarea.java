package com.example.EduvibeBackend.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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

    private String nombreTarea;
    private String enunciado;
    private LocalDate fechaApertura; // Cambiado de Date a LocalDate
    private Boolean estado;
    private Double calificacion;
    
    @ElementCollection
    @Lob
    private List<byte[]> archivoAdjunto; // Lista para archivos adjuntos

    @ManyToOne
    @JoinColumn(name = "id_clase")
    private Clase clase;
    
    @PrePersist
    protected void onCreate() {
        this.fechaApertura = LocalDate.now();
    }
}
