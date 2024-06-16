package com.example.EduvibeBackend.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Clase que representa una clase en el sistema educativo.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "clases")
public class Clase {

    /**
     * Identificador único de la clase.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClase;

    /**
     * Nombre de la clase.
     */
    @NotNull
    private String nombre;

    /**
     * Descripción de la clase.
     */
    @NotNull
    private String descripcion;

    /**
     * Conjunto de usuarios inscritos en la clase.
     */
    @ManyToMany(mappedBy = "clases")
    @JsonIgnore
    private Set<User> usuarios = new HashSet<>();

    /**
     * Lista de tareas asociadas a la clase.
     */
    @OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> tareas;

    /**
     * Compara esta clase con el objeto especificado. La comparación se basa en el campo `idClase`.
     *
     * @param o el objeto a comparar
     * @return true si los objetos son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clase clase = (Clase) o;
        return Objects.equals(idClase, clase.idClase);
    }

    /**
     * Calcula el código hash para esta clase. El cálculo se basa en el campo `idClase`.
     *
     * @return el código hash de esta clase
     */
    @Override
    public int hashCode() {
        return Objects.hash(idClase);
    }
}
