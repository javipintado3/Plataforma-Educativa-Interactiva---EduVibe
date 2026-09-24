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
 * Examen de opción múltiple de una clase.
 *
 * Aparte de {@link Assignment} a propósito: un examen se corrige solo y se
 * responde contrarreloj, una tarea no. Sus preguntas viven en
 * {@link ExamQuestion} y los intentos del alumnado en {@link ExamAttempt},
 * igual que una tarea y sus entregas están en tablas separadas.
 */
@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
public class Exam {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    /** Tema al que pertenece. Opcional: un examen puede ir suelto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    /** Última fecha en la que se puede empezar el examen. Null si no hay límite. */
    @Column(name = "due_date")
    private Instant dueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public Exam(SchoolClass schoolClass, String title, String description,
               int durationMinutes, Instant dueDate, User createdBy) {
        this.schoolClass = schoolClass;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.dueDate = dueDate;
        this.createdBy = createdBy;
    }

    /** Si ya ha pasado la fecha límite para empezarlo. Sin fecha, siempre se puede empezar. */
    public boolean haVencido() {
        return dueDate != null && Instant.now().isAfter(dueDate);
    }
}
