package com.eduvibe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.assignment.AssignmentDetailResponse;
import com.eduvibe.dto.assignment.AssignmentResponse;
import com.eduvibe.dto.assignment.SaveAssignmentRequest;
import com.eduvibe.exception.BadRequestException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.Enrollment;
import com.eduvibe.model.Notification;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.Submission;
import com.eduvibe.model.Topic;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.EnrollmentRole;
import com.eduvibe.repository.AssignmentRepository;
import com.eduvibe.repository.EnrollmentRepository;
import com.eduvibe.repository.SubmissionRepository;
import com.eduvibe.repository.TopicRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Tareas de una clase.
 *
 * Quién puede crearlas y editarlas lo decide {@link ClassAccessService}: hay
 * que ser profesor de esa clase concreta, o administración. Tener rol de
 * profesor en el centro no basta para poner tareas en la clase de otro.
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;
    private final SubmissionService submissionService;
    private final NotificationService notificationService;

    @Transactional
    public AssignmentDetailResponse crear(UUID classId, SaveAssignmentRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);
        AuthenticatedUser autenticado = authService.identidadActual();

        User autor = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        Assignment tarea = new Assignment(clase, peticion.title().trim(), peticion.description(),
                peticion.dueDate(), peticion.puntosOPorDefecto(), autor);
        tarea.setTopic(temaDeLaClase(peticion.topicId(), classId));

        assignmentRepository.saveAndFlush(tarea);
        notificarAlumnado(classId, clase, Notification.TAREA_NUEVA, tarea.getTitle());

        return AssignmentDetailResponse.de(tarea, true, null);
    }

    /** Avisa a todo el alumnado matriculado de que hay una novedad con este título. */
    private void notificarAlumnado(UUID classId, SchoolClass clase, String tipo, String titulo) {
        List<User> alumnado = enrollmentRepository
                .findBySchoolClassIdAndRoleInClassOrderByUserNameAsc(classId, EnrollmentRole.STUDENT)
                .stream().map(Enrollment::getUser).toList();

        notificationService.emitirParaVarios(alumnado, tipo, Map.of("className", clase.getName(), "title", titulo));
    }

    /**
     * Trabajo de clase.
     *
     * Lo que se devuelve depende de quién mire: al alumnado, el estado de su
     * propia entrega; al profesorado, cuántas lleva recibidas cada tarea. Las
     * entregas del alumno se consultan todas juntas, no una por tarea.
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> listar(UUID classId) {
        acceso.exigirVisible(classId);
        AuthenticatedUser usuario = authService.identidadActual();

        List<Assignment> tareas = assignmentRepository.findDeClase(classId);
        if (tareas.isEmpty()) {
            return List.of();
        }

        if (acceso.puedeCalificarEn(classId)) {
            return tareas.stream()
                    .map(t -> AssignmentResponse.paraProfesor(t,
                            submissionRepository.countByAssignmentId(t.getId())))
                    .toList();
        }

        List<UUID> ids = tareas.stream().map(Assignment::getId).toList();

        Map<UUID, Submission> misEntregas = new HashMap<>();
        for (Submission entrega : submissionRepository.findDeAlumnoEnTareas(usuario.id(), ids)) {
            misEntregas.put(entrega.getAssignment().getId(), entrega);
        }

        return tareas.stream().map(tarea -> {
            Submission mia = misEntregas.get(tarea.getId());
            return AssignmentResponse.paraAlumno(
                    tarea,
                    mia == null ? null : mia.getStatus().getValor(),
                    mia == null ? null : mia.entregadaTarde());
        }).toList();
    }

    @Transactional(readOnly = true)
    public AssignmentDetailResponse detalle(UUID assignmentId) {
        Assignment tarea = buscarVisible(assignmentId);
        UUID classId = tarea.getSchoolClass().getId();

        boolean puedoEditar = acceso.puedeCalificarEn(classId);

        // Al profesorado no se le adjunta "su" entrega: no la tiene
        var miEntrega = puedoEditar ? null : submissionService.miEntregaSiExiste(tarea);

        return AssignmentDetailResponse.de(tarea, puedoEditar, miEntrega);
    }

    @Transactional
    public AssignmentDetailResponse actualizar(UUID assignmentId, SaveAssignmentRequest peticion) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        UUID classId = tarea.getSchoolClass().getId();
        acceso.exigirEditable(classId);

        tarea.setTitle(peticion.title().trim());
        tarea.setDescription(peticion.description());
        tarea.setDueDate(peticion.dueDate());
        tarea.setPoints(peticion.puntosOPorDefecto());
        tarea.setTopic(temaDeLaClase(peticion.topicId(), classId));

        assignmentRepository.save(tarea);

        return AssignmentDetailResponse.de(tarea, true, null);
    }

    /**
     * Borra una tarea. Se lleva consigo sus entregas y sus notas, por las
     * cascadas de la base de datos, así que solo se permite mientras nadie haya
     * entregado nada: borrar trabajo corregido no debe ser un descuido posible.
     */
    @Transactional
    public void eliminar(UUID assignmentId) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        acceso.exigirEditable(tarea.getSchoolClass().getId());

        long entregas = submissionRepository.countByAssignmentId(assignmentId);
        if (entregas > 0) {
            throw new BadRequestException(
                    "No se puede borrar: ya hay " + entregas + " entrega(s). Edítala o retírala del tema.");
        }

        assignmentRepository.delete(tarea);
    }

    /** Busca una tarea comprobando que quien pregunta pueda ver su clase. */
    @Transactional(readOnly = true)
    public Assignment buscarVisible(UUID assignmentId) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        acceso.exigirVisible(tarea.getSchoolClass().getId());
        return tarea;
    }

    /**
     * Comprueba que el tema indicado pertenece a esta clase.
     *
     * Sin esta comprobación se podría colgar una tarea de un tema de otra clase
     * sin más que conocer su identificador.
     */
    private Topic temaDeLaClase(UUID topicId, UUID classId) {
        if (topicId == null) {
            return null;
        }
        return topicRepository.findByIdAndSchoolClassId(topicId, classId)
                .orElseThrow(() -> new BadRequestException("El tema indicado no es de esta clase"));
    }
}
