package com.eduvibe.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.rubric.RubricScoreInput;
import com.eduvibe.dto.rubric.RubricScoreResponse;
import com.eduvibe.dto.submission.GradeRequest;
import com.eduvibe.dto.submission.GradeResponse;
import com.eduvibe.dto.submission.SubmissionResponse;
import com.eduvibe.dto.submission.SubmitRequest;
import com.eduvibe.dto.submission.TeacherNoteRequest;
import com.eduvibe.exception.BadRequestException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.ClassGroup;
import com.eduvibe.model.ClassGroupMember;
import com.eduvibe.model.Grade;
import com.eduvibe.model.Notification;
import com.eduvibe.model.Rubric;
import com.eduvibe.model.RubricCriterion;
import com.eduvibe.model.RubricScore;
import com.eduvibe.model.Submission;
import com.eduvibe.model.User;
import com.eduvibe.repository.AssignmentRepository;
import com.eduvibe.repository.ClassGroupMemberRepository;
import com.eduvibe.repository.GradeRepository;
import com.eduvibe.repository.RubricCriterionRepository;
import com.eduvibe.repository.RubricScoreRepository;
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
    private final RubricService rubricService;
    private final RubricCriterionRepository rubricCriterionRepository;
    private final RubricScoreRepository rubricScoreRepository;
    private final ClassGroupMemberRepository classGroupMemberRepository;

    /**
     * Crea o actualiza la entrega de quien está autenticado.
     *
     * Un mismo endpoint sirve para guardar el borrador y para enviar, porque es
     * literalmente el mismo gesto con una casilla distinta, y tener dos rutas
     * obligaría al cliente a decidir cuál llamar en cada pulsación.
     *
     * En una tarea grupal, lo entregado no es de quien pulsa "Entregar" sino de
     * todo su subgrupo: se guarda la misma fila (contenido, archivo, momento
     * del envío) en la entrega de cada miembro, no solo en la propia. Así el
     * resto del modelo —una entrega por alumno y tarea, una nota por entrega—
     * no necesita saber que existen los subgrupos.
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

        ClassGroup grupo = null;
        List<User> destinatarios = List.of(alumno);

        if (tarea.isGroupAssignment()) {
            ClassGroupMember membresia = classGroupMemberRepository.findDeAlumnoEnClase(alumno.getId(), classId)
                    .orElseThrow(() -> new BadRequestException(
                            "No perteneces a ningún subgrupo de esta clase; pide al profesorado que te añada a uno."));
            grupo = membresia.getClassGroup();
            destinatarios = classGroupMemberRepository.findByClassGroupId(grupo.getId()).stream()
                    .map(ClassGroupMember::getUser)
                    .toList();
        }

        // Mismo instante para todos los miembros: si se calculara por separado,
        // "llegó tarde" podría dar una respuesta distinta según a quién se le
        // preguntase por su propia fila.
        Instant momento = Instant.now();
        Submission miEntrega = null;

        for (User miembro : destinatarios) {
            Submission entrega = submissionRepository
                    .findByAssignmentIdAndStudentId(assignmentId, miembro.getId())
                    .orElseGet(() -> new Submission(tarea, miembro));

            // Una vez corregida, ya no se puede cambiar lo entregado: la nota
            // dejaría de corresponder a lo que se calificó
            if (entrega.estaCalificada()) {
                throw new BadRequestException("Esta entrega ya está corregida y no se puede modificar");
            }

            entrega.setClassGroup(grupo);
            if (peticion.quiereEnviar()) {
                entrega.enviar(peticion.content(), peticion.fileUrl(), momento);
            } else {
                entrega.guardarBorrador(peticion.content(), peticion.fileUrl());
            }
            submissionRepository.saveAndFlush(entrega);

            if (miembro.getId().equals(alumno.getId())) {
                miEntrega = entrega;
            }
        }

        return SubmissionResponse.de(miEntrega, null);
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
     * Si la tarea tiene rúbrica, la nota en bruto es la suma de lo puntuado en
     * cada criterio y no lo que venga en {@code peticion.score()}: con rúbrica,
     * la nota es una consecuencia de puntuar los criterios, no un número aparte
     * que alguien podría dejar sin corresponderse con ellos.
     *
     * En una tarea grupal, calificar cualquier entrega del subgrupo califica a
     * todo el subgrupo con la misma nota: no tendría sentido que dos compañeros
     * que entregaron lo mismo sacasen notas distintas por corregirlos por
     * separado.
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

        Rubric rubrica = rubricService.buscarDe(tarea.getId()).orElse(null);
        AuthenticatedUser autenticado = authService.identidadActual();
        User corrector = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        GradeResponse notaOrigen = calificarUna(entrega, tarea, rubrica, peticion, corrector);

        if (entrega.getClassGroup() != null) {
            List<Submission> companeras = submissionRepository.findByAssignmentIdAndClassGroupIdAndIdNot(
                    tarea.getId(), entrega.getClassGroup().getId(), entrega.getId());
            for (Submission companera : companeras) {
                calificarUna(companera, tarea, rubrica, peticion, corrector);
            }
        }

        return SubmissionResponse.de(entrega, notaOrigen);
    }

    /** Califica una única entrega: valida el máximo, aplica la penalización y guarda la nota (y su rúbrica, si toca). */
    private GradeResponse calificarUna(Submission entrega, Assignment tarea, Rubric rubrica, GradeRequest peticion,
                                       User corrector) {
        BigDecimal notaEnBruto = rubrica != null
                ? sumaDeRubrica(rubrica, peticion.rubricScoresOSinNinguna())
                : notaSuelta(peticion);

        BigDecimal maximo = BigDecimal.valueOf(tarea.getPoints());
        if (notaEnBruto.compareTo(maximo) > 0) {
            throw new BadRequestException("La nota no puede pasar de " + maximo + ", que es lo que vale la tarea");
        }

        // El profesorado pone la nota sobre el trabajo entregado; si llegó tarde
        // y la tarea tiene penalización configurada, el descuento se aplica aquí
        // y no a mano, para que nunca dependa de que alguien se acuerde.
        boolean penalizacionAplicada = entrega.entregadaTarde() && tarea.getLatePenaltyPercent() > 0;
        BigDecimal notaFinal = tarea.aplicarPenalizacionSiProcede(notaEnBruto, entrega.entregadaTarde());

        Grade nota = gradeRepository.findBySubmissionId(entrega.getId())
                .orElseGet(() -> new Grade(entrega, notaEnBruto, notaFinal, peticion.feedback(), corrector));

        // Si ya existía, se actualiza dejando constancia de quién revisa
        nota.corregir(notaEnBruto, notaFinal, peticion.feedback(), corrector, penalizacionAplicada);
        gradeRepository.saveAndFlush(nota);

        List<RubricScoreResponse> puntuacionesRubrica = rubrica != null
                ? guardarPuntuacionesDeRubrica(rubrica, nota, peticion.rubricScoresOSinNinguna())
                : List.of();

        entrega.marcarComoCalificada();
        submissionRepository.save(entrega);

        notificationService.emitir(entrega.getStudent(), Notification.NOTA_PUBLICADA, Map.of(
                "title", tarea.getTitle(), "className", tarea.getSchoolClass().getName(),
                "assignmentId", tarea.getId().toString()));

        return GradeResponse.de(nota, puntuacionesRubrica);
    }

    private BigDecimal notaSuelta(GradeRequest peticion) {
        if (peticion.score() == null) {
            throw new BadRequestException("La nota es obligatoria");
        }
        return peticion.score();
    }

    /** Suma lo puntuado en cada criterio, comprobando que cada uno respeta su propio máximo. */
    private BigDecimal sumaDeRubrica(Rubric rubrica, List<RubricScoreInput> entradas) {
        if (entradas.isEmpty()) {
            throw new BadRequestException("Esta tarea tiene rúbrica: puntúa cada criterio");
        }

        Map<UUID, RubricCriterion> criterios = rubricCriterionRepository
                .findByRubricIdOrderBySortOrderAsc(rubrica.getId())
                .stream()
                .collect(Collectors.toMap(RubricCriterion::getId, c -> c));

        if (entradas.size() != criterios.size()) {
            throw new BadRequestException("Puntúa todos los criterios de la rúbrica, ni más ni menos");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (RubricScoreInput entrada : entradas) {
            RubricCriterion criterio = criterios.get(entrada.criterionId());
            if (criterio == null) {
                throw new BadRequestException("Ese criterio no pertenece a la rúbrica de esta tarea");
            }
            if (entrada.points().compareTo(criterio.getMaxPoints()) > 0) {
                throw new BadRequestException(
                        "\"" + criterio.getDescription() + "\" vale como mucho " + criterio.getMaxPoints());
            }
            total = total.add(entrada.points());
        }
        return total;
    }

    /** Reemplaza las puntuaciones de rúbrica de esta nota por las nuevas. */
    private List<RubricScoreResponse> guardarPuntuacionesDeRubrica(Rubric rubrica, Grade nota,
                                                                    List<RubricScoreInput> entradas) {
        rubricScoreRepository.deleteByGradeId(nota.getId());

        Map<UUID, RubricCriterion> criterios = rubricCriterionRepository
                .findByRubricIdOrderBySortOrderAsc(rubrica.getId())
                .stream()
                .collect(Collectors.toMap(RubricCriterion::getId, c -> c));

        List<RubricScore> puntuaciones = new ArrayList<>();
        for (RubricScoreInput entrada : entradas) {
            puntuaciones.add(new RubricScore(nota, criterios.get(entrada.criterionId()), entrada.points()));
        }
        rubricScoreRepository.saveAll(puntuaciones);

        return puntuaciones.stream().map(RubricScoreResponse::de).toList();
    }

    /**
     * Deja o cambia la nota rápida del profesorado, independiente de la
     * calificación: no exige que la entrega ya esté enviada ni calificada,
     * porque su gracia es poder avisar de algo antes de llegar a ese punto.
     */
    @Transactional
    public SubmissionResponse comentar(UUID submissionId, TeacherNoteRequest peticion) {
        Submission entrega = submissionRepository.findById(submissionId)
                .orElseThrow(() -> NotFoundException.de("Entrega", submissionId));

        acceso.exigirEditable(entrega.getAssignment().getSchoolClass().getId());

        entrega.setTeacherNote(peticion.teacherNote());
        submissionRepository.save(entrega);

        return SubmissionResponse.de(entrega, notaDe(entrega));
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
        List<Grade> notasEncontradas = gradeRepository.findDeEntregas(ids);

        Map<UUID, List<RubricScoreResponse>> rubricasPorNota = rubricasDe(notasEncontradas);

        Map<UUID, GradeResponse> notas = new HashMap<>();
        for (Grade nota : notasEncontradas) {
            notas.put(nota.getSubmission().getId(),
                    GradeResponse.de(nota, rubricasPorNota.getOrDefault(nota.getId(), List.of())));
        }

        return entregas.stream()
                .map(entrega -> SubmissionResponse.de(entrega, notas.get(entrega.getId())))
                .toList();
    }

    private GradeResponse notaDe(Submission entrega) {
        return gradeRepository.findBySubmissionId(entrega.getId())
                .map(nota -> GradeResponse.de(nota, rubricasDe(List.of(nota)).getOrDefault(nota.getId(), List.of())))
                .orElse(null);
    }

    /** El desglose por criterio de varias notas de golpe, en lugar de una consulta por nota. */
    private Map<UUID, List<RubricScoreResponse>> rubricasDe(List<Grade> notas) {
        if (notas.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = notas.stream().map(Grade::getId).toList();

        Map<UUID, List<RubricScoreResponse>> porNota = new HashMap<>();
        for (RubricScore puntuacion : rubricScoreRepository.findByGradeIdIn(ids)) {
            porNota.computeIfAbsent(puntuacion.getGrade().getId(), k -> new ArrayList<>())
                    .add(RubricScoreResponse.de(puntuacion));
        }
        return porNota;
    }
}
