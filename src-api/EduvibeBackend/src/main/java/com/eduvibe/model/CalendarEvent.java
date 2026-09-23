package com.eduvibe.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.eduvibe.model.enums.CalendarEventType;

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
 * Evento introducido a mano en el calendario: un examen, un festivo, una salida.
 *
 * Las fechas de entrega NO se guardan aquí. Se derivan de assignments.due_date
 * al construir la agenda, para que cambiar el plazo de una tarea no deje un
 * evento antiguo apuntando a la fecha vieja.
 */
@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@NoArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    /** Null si el evento es de todo el centro y no de una clase concreta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Column(name = "type", length = 20)
    private CalendarEventType type;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public CalendarEvent(Organization organization, SchoolClass schoolClass,
                         String title, Instant eventDate, CalendarEventType type) {
        this.organization = organization;
        this.schoolClass = schoolClass;
        this.title = title;
        this.eventDate = eventDate;
        this.type = type;
    }
}
