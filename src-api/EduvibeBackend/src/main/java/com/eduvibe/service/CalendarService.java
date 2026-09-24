package com.eduvibe.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.calendar.CalendarEntryResponse;
import com.eduvibe.dto.calendar.SaveCalendarEventRequest;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.CalendarEvent;
import com.eduvibe.model.Organization;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.enums.CalendarEventType;
import com.eduvibe.repository.AssignmentRepository;
import com.eduvibe.repository.CalendarEventRepository;
import com.eduvibe.repository.OrganizationRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Agenda del centro: eventos introducidos a mano y fechas de entrega.
 *
 * Las dos fuentes se unifican en {@link CalendarEntryResponse} y se devuelven
 * ya mezcladas y ordenadas, para que el cliente no tenga que combinar dos
 * listas. Ver quién ve qué es la misma regla en las dos consultas: todo el
 * centro para administración, lo propio de todo el centro más las clases en
 * las que se participa para el resto.
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarEventRepository calendarEventRepository;
    private final AssignmentRepository assignmentRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<CalendarEntryResponse> agenda(Instant desde, Instant hasta) {
        AuthenticatedUser usuario = authService.identidadActual();
        UUID orgId = usuario.organizationId();

        List<CalendarEvent> eventos;
        List<Assignment> tareas;

        if (usuario.esAdmin()) {
            eventos = calendarEventRepository.findByOrganizationIdAndEventDateBetweenOrderByEventDateAsc(
                    orgId, desde, hasta);
            tareas = assignmentRepository.findConEntregaDeOrganizacion(orgId, desde, hasta);
        } else {
            eventos = calendarEventRepository.findParaUsuario(orgId, usuario.id(), desde, hasta);
            tareas = assignmentRepository.findConEntregaParaUsuario(orgId, usuario.id(), desde, hasta);
        }

        return Stream.concat(
                        eventos.stream().map(CalendarEntryResponse::deEvento),
                        tareas.stream().map(CalendarEntryResponse::deTarea))
                .sorted(Comparator.comparing(CalendarEntryResponse::fecha))
                .toList();
    }

    @Transactional
    public CalendarEntryResponse crear(SaveCalendarEventRequest peticion) {
        AuthenticatedUser usuario = authService.identidadActual();
        SchoolClass clase = claseDelEvento(peticion.classId(), usuario);

        Organization organizacion = organizationRepository.findById(usuario.organizationId())
                .orElseThrow(() -> NotFoundException.de("Organización", usuario.organizationId()));

        CalendarEvent evento = new CalendarEvent(organizacion, clase, peticion.title().trim(),
                peticion.eventDate(), tipoOSinTipo(peticion.type()));
        calendarEventRepository.saveAndFlush(evento);

        return CalendarEntryResponse.deEvento(evento);
    }

    @Transactional
    public CalendarEntryResponse actualizar(UUID eventId, SaveCalendarEventRequest peticion) {
        CalendarEvent evento = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> NotFoundException.de("Evento", eventId));
        exigirPuedeGestionar(evento);

        // La clase del evento no se toca aquí: cambiarlo de clase, o de clase a
        // todo el centro, cambia quién puede gestionarlo, así que para eso se
        // borra y se crea de nuevo en lugar de reinterpretar la petición.
        evento.setTitle(peticion.title().trim());
        evento.setEventDate(peticion.eventDate());
        evento.setType(tipoOSinTipo(peticion.type()));
        calendarEventRepository.save(evento);

        return CalendarEntryResponse.deEvento(evento);
    }

    @Transactional
    public void eliminar(UUID eventId) {
        CalendarEvent evento = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> NotFoundException.de("Evento", eventId));
        exigirPuedeGestionar(evento);

        calendarEventRepository.delete(evento);
    }

    /** Un evento de clase lo gestiona su profesorado o administración; uno de todo el centro, solo administración. */
    private SchoolClass claseDelEvento(UUID classId, AuthenticatedUser usuario) {
        if (classId == null) {
            if (!usuario.esAdmin()) {
                throw new AccessDeniedException("Solo la administración crea eventos de todo el centro");
            }
            return null;
        }
        return acceso.exigirEditable(classId);
    }

    private void exigirPuedeGestionar(CalendarEvent evento) {
        SchoolClass clase = evento.getSchoolClass();
        if (clase == null) {
            if (!authService.identidadActual().esAdmin()) {
                throw new AccessDeniedException("Solo la administración gestiona los eventos de todo el centro");
            }
        } else {
            acceso.exigirEditable(clase.getId());
        }
    }

    private CalendarEventType tipoOSinTipo(String type) {
        return (type == null || type.isBlank()) ? null : CalendarEventType.desdeValor(type);
    }
}
