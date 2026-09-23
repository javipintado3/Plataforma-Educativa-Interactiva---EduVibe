package com.eduvibe.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una clase: un grupo de alumnos con uno o varios profesores, sus tareas y sus
 * materiales.
 *
 * Se llama SchoolClass y no Class porque Class ya existe en java.lang y
 * trabajar con las dos en el mismo fichero sería una fuente constante de
 * confusión. La tabla sí se llama classes.
 *
 * No tiene columna de profesor: quién la imparte se responde consultando
 * enrollments con role_in_class = 'teacher'. Así hay una sola fuente de verdad
 * y se admiten varios profesores en la misma clase.
 */
@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
public class SchoolClass {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    /** Grupo, por ejemplo "1º Bachillerato A". */
    @Column(name = "name", nullable = false)
    private String name;

    /** Asignatura, por ejemplo "Matemáticas". */
    @Column(name = "subject")
    private String subject;

    /** Color con el que se identifica la clase en toda la interfaz. */
    @Column(name = "color", length = 20)
    private String color;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public SchoolClass(Organization organization, String name, String subject, String color) {
        this.organization = organization;
        this.name = name;
        this.subject = subject;
        this.color = color;
    }
}
