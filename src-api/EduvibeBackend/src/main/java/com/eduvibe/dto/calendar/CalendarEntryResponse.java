package com.eduvibe.dto.calendar;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.Assignment;
import com.eduvibe.model.CalendarEvent;

/**
 * Una entrada de la agenda.
 *
 * Unifica dos orígenes distintos: los eventos introducidos a mano y las fechas
 * de entrega, que no se guardan como eventos sino que se derivan de las tareas.
 * Al cliente le llegan con la misma forma, y así no tiene que mezclar dos
 * listas ni ordenarlas por su cuenta.
 *
 * @param origen     'event' o 'assignment'
 * @param referencia identificador del evento o de la tarea, para poder navegar
 */
public record CalendarEntryResponse(
        UUID referencia,
        String origen,
        String title,
        Instant fecha,
        String tipo,
        UUID classId,
        String className,
        String classSubject,
        String classColor) {

    public static CalendarEntryResponse deEvento(CalendarEvent evento) {
        var clase = evento.getSchoolClass();
        return new CalendarEntryResponse(
                evento.getId(),
                "event",
                evento.getTitle(),
                evento.getEventDate(),
                evento.getType() == null ? "other" : evento.getType().getValor(),
                clase == null ? null : clase.getId(),
                clase == null ? null : clase.getName(),
                clase == null ? null : clase.getSubject(),
                clase == null ? null : clase.getColor());
    }

    public static CalendarEntryResponse deTarea(Assignment tarea) {
        var clase = tarea.getSchoolClass();
        return new CalendarEntryResponse(
                tarea.getId(),
                "assignment",
                tarea.getTitle(),
                tarea.getDueDate(),
                "assignment_due",
                clase.getId(),
                clase.getName(),
                clase.getSubject(),
                clase.getColor());
    }
}
