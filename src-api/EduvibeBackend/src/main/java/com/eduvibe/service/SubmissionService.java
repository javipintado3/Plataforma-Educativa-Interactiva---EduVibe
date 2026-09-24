package com.eduvibe.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.submission.GradeRequest;
import com.eduvibe.dto.submission.GradeResponse;
import com.eduvibe.dto.submission.SubmissionResponse;
import com.eduvibe.dto.submission.SubmitRequest;
import com.eduvibe.exception.BadRequestException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.Grade;
import com.eduvibe.model.Notification;
import com.eduvibe.model.Submission;
import com.eduvibe.model.User;
import com.eduvibe.repository.AssignmentRepository;
import com.eduvibe.repository.GradeRepository;
import com.eduvibe.repository.SubmissionRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Entregas del alumnado y su corrección.
 *
 * No depende de AssignmentService a propósito: si lo hiciera, y aquel depende
 * de este para adjuntar la entrega propia al detalle de una tarea, Spring no
 * podría construir ninguno de los dos.
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;
    private final NotificationService notificationService;

    /**
     * Crea o actualiza la entrega de quien está autenticado.
     *
     * Un mismo endpoint sirve para guardar el borrador y para enviar, porque es
     * literalmente el mismo gesto con una casilla distinta, y tener dos rutas
     * obligaría al cliente a decidir cuál llamar en cada pulsación.
     */
    @Transactional
    public SubmissionResponse guardarMiEntrega(UUID assignmentId, SubmitRequest peticion) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        UUID classId = tarea.getSchoolClass().getId();
        acceso.exigirSerAlumnoDe(classId);

        AuthenticatedUser autenticado = authService.identidadActual();
        User alumno = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        Submission entrega = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, alumno.getId())
                .orElseGet(() -> new Submission(tarea, alumno));

        // Una vez corregida, el alumno ya no puede cambiar lo entregado: la nota
        // dejaría de corresponder a lo que se calificó
        if (entrega.estaCalificada()) {
            throw new BadRequestException("Esta entrega ya está corregida y no se puede modificar");
        }

        if (peticion.quiereEnviar()) {
            entrega.enviar(peticion.content(), peticion.fileUrl());
        } else {
            entrega.guardarBorrador(peticion.content(), peticion.fileUrl());
        }

        submissionRepository.saveAndFlush(entrega);

        return SubmissionResponse.de(entrega, null);
    }

    /** La entrega de quien consulta para una tarea, o null si aún no tiene. */
    @Transactional(readOnly = true)
    public SubmissionResponse miEntregaSiExiste(Assignment tarea) {
        AuthenticatedUser usuario = authService.identidadActual();

        return submissionRepository
                .findByAssignmentIdAndStudentId(tarea.getId(), usuario.id())
                .map(entrega -> SubmissionResponse.de(entrega, notaDe(entrega)))
                .orElse(null);
    }

    /**
     * Todas las entregas de una tarea, para la pantalla de corrección.
     *
     * Las notas se traen en una sola consulta en lugar de una por entrega.
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> listarDeTarea(UUID assignmentId) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        acceso.exigirEditable(tarea.getSchoolClass().getId());

        List<Submission> entregas = submissionRepository.findByAssignmentIdOrderByStudentNameAsc(assignmentId);
        return conSusNotas(entregas);
    }

    /**
     * Pone o corrige la nota de una entrega.
     *
     * Calificar un borrador no tiene sentido: todavía no se ha entregado nada.
     */
    @Transactional
    public SubmissionResponse calificar(UUID submissionId, GradeRequest peticion) {
        Submission entrega = submissionRepository.findById(submissionId)
                .orElseThrow(() -> NotFoundException.de("Entrega", submissionId));

        Assignment tarea = entrega.getAssignment();
        acceso.exigirEditable(tarea.getSchoolClass().getId());

        if (entrega.esBorrador()) {
            throw new BadRequestException("No se puede calificar una entrega que todavía es un borrador");
        }

        BigDecimal maximo = BigDecimal.valueOf(tarea.getPoints());
        if (peticion.score().compareTo(maximo) > 0) {
            throw new BadRequestException("La nota no puede pasar de " + maximo + ", que es lo que vale la tarea");
        }

        AuthenticatedUser autenticado = authService.identidadActual();
        User corrector = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        Grade nota = gradeRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> new Grade(entrega, peticion.score(), peticion.feedback(), corrector));

        // Si ya existía, se actualiza dejando constancia de quién revisa
        nota.corregir(peticion.score(), peticion.feedback(), corrector);
        gradeRepository.saveAndFlush(nota);

        entrega.marcarComoCalificada();
        submissionRepository.save(entrega);

        notificationService.emitir(entrega.getStudent(), Notification.NOTA_PUBLICADA,
                Map.of("title", tarea.getTitle(), "className", tarea.getSchoolClass().getName()));

        return SubmissionResponse.de(entrega, GradeResponse.de(nota));
    }

    /** Las entregas de quien consulta en una clase: su pestaña de calificaciones. */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> misEntregasDeClase(UUID classId) {
        acceso.exigirVisible(classId);
        AuthenticatedUser usuario = authService.identidadActual();

        return conSusNotas(submissionRepository.findDeAlumnoEnClase(usuario.id(), classId));
    }

    /**
     * Empareja cada entrega con su nota trayéndolas todas de golpe, en lugar de
     * consultar la nota dentro del bucle.
     */
    private List<SubmissionResponse> conSusNotas(List<Submission> entregas) {
        if (entregas.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = entregas.stream().map(Submission::getId).toList();

        Map<UUID, GradeResponse> notas = new HashMap<>();
        for (Grade nota : gradeRepository.findDeEntregas(ids)) {
            notas.put(nota.getSubmission().getId(), GradeResponse.de(nota));
        }

        return entregas.stream()
                .map(entrega -> SubmissionResponse.de(entrega, notas.get(entrega.getId())))
                .toList();
    }

    private GradeResponse notaDe(Submission entrega) {
        return gradeRepository.findBySubmissionId(entrega.getId())
                .map(GradeResponse::de)
                .orElse(null);
    }
}
