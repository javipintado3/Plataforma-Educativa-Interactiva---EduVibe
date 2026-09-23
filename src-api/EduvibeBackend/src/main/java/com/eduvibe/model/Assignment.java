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
 * Tarea propuesta a una clase.
 *
 * Es una sola fila por tarea, no una por alumno. Lo que cada alumno hace con
 * ella vive en {@link Submission}. En el modelo anterior enunciado, entrega y
 * nota compartían fila, de modo que una tarea para veinte alumnos eran veinte
 * copias del enunciado y no existía "la tarea" como tal.
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    /** Tema al que pertenece. Opcional: una tarea puede ir suelta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    /** Fecha límite. Null si la tarea no tiene plazo. */
    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "points", nullable = false)
    private int points;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public Assignment(SchoolClass schoolClass, String title, String description,
                      Instant dueDate, int points, User createdBy) {
        this.schoolClass = schoolClass;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.points = points;
        this.createdBy = createdBy;
    }

    /** Si ya ha pasado el plazo. Una tarea sin fecha nunca vence. */
    public boolean haVencido() {
        return dueDate != null && Instant.now().isAfter(dueDate);
    }

    /**
     * Si una entrega enviada en ese momento llegaría fuera de plazo.
     *
     * Vive aquí, y no en una columna de submissions, porque es una consecuencia
     * de comparar dos fechas: guardarlo sería tener el mismo dato dos veces y
     * exponerse a que dejen de coincidir cuando se cambia la fecha límite.
     */
    public boolean seEntregaTarde(Instant momentoDeEntrega) {
        return dueDate != null && momentoDeEntrega != null && momentoDeEntrega.isAfter(dueDate);
    }
}
