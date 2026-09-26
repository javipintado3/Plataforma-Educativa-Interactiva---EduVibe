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
 * Subgrupo reutilizable dentro de una clase: un equipo de trabajo.
 *
 * Lo gestiona el profesorado (quién está en cada uno) y sirve tanto para
 * organizar desdobles como, sobre todo, para las tareas grupales: quien
 * entrega una tarea grupal lo hace en nombre de su subgrupo, no a título
 * individual. Sus miembros viven en {@link ClassGroupMember}.
 */
@Entity
@Table(name = "class_groups")
@Getter
@Setter
@NoArgsConstructor
public class ClassGroup {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "name", nullable = false)
    private String name;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ClassGroup(SchoolClass schoolClass, String name) {
        this.schoolClass = schoolClass;
        this.name = name;
    }
}
