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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rúbrica de una tarea: cómo se reparte la puntuación entre sus criterios.
 *
 * Es 1-a-1 con {@link Assignment} y no una lista de criterios sueltos en la
 * propia tarea porque una tarea puede no tener rúbrica en absoluto —la
 * mayoría no la tienen— y así el caso normal no arrastra una tabla vacía.
 */
@Entity
@Table(name = "rubrics")
@Getter
@Setter
@NoArgsConstructor
public class Rubric {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, unique = true)
    private Assignment assignment;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Rubric(Assignment assignment) {
        this.assignment = assignment;
    }
}
