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
 * Tema o bloque dentro de una clase. Agrupa tareas y materiales, de forma que
 * el trabajo de clase no sea una lista plana sino algo ordenado por unidades o
 * por semanas.
 */
@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
public class Topic {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "title", nullable = false)
    private String title;

    /** Orden de aparición dentro de la clase. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Topic(SchoolClass schoolClass, String title, int sortOrder) {
        this.schoolClass = schoolClass;
        this.title = title;
        this.sortOrder = sortOrder;
    }
}
