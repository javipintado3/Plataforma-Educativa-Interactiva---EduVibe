package com.eduvibe.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduvibe.dto.announcement.AnnouncementResponse;
import com.eduvibe.dto.announcement.SaveAnnouncementRequest;
import com.eduvibe.dto.assignment.AssignmentDetailResponse;
import com.eduvibe.dto.assignment.AssignmentResponse;
import com.eduvibe.dto.assignment.SaveAssignmentRequest;
import com.eduvibe.dto.exam.BankQuestionResponse;
import com.eduvibe.dto.exam.ExamDetailResponse;
import com.eduvibe.dto.exam.ExamResponse;
import com.eduvibe.dto.exam.SaveExamRequest;
import com.eduvibe.dto.schoolclass.ClassDetailResponse;
import com.eduvibe.dto.schoolclass.ClassResponse;
import com.eduvibe.dto.schoolclass.CreateClassRequest;
import com.eduvibe.dto.schoolclass.CreateTopicRequest;
import com.eduvibe.dto.schoolclass.EnrollRequest;
import com.eduvibe.dto.schoolclass.MemberResponse;
import com.eduvibe.dto.resource.ResourceResponse;
import com.eduvibe.dto.resource.SaveResourceRequest;
import com.eduvibe.dto.schoolclass.TopicResponse;
import com.eduvibe.dto.submission.SubmissionResponse;
import com.eduvibe.service.AnnouncementService;
import com.eduvibe.service.AssignmentService;
import com.eduvibe.service.ExamService;
import com.eduvibe.service.ResourceService;
import com.eduvibe.service.SchoolClassService;
import com.eduvibe.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Clases y todo lo que cuelga de una: personas, temas, tareas y calificaciones.
 *
 * Los permisos no se declaran aquí sino en los servicios, a través de
 * {@link com.eduvibe.service.ClassAccessService}, porque dependen de la
 * relación entre la persona y esa clase concreta y no solo de su rol: una regla
 * como hasRole('TEACHER') dejaría a cualquier profesor del centro poner tareas
 * en la clase de otro.
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService schoolClassService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final AnnouncementService announcementService;
    private final ResourceService resourceService;
    private final ExamService examService;

    // ---------------------------------------------------------------- clases

    /** Panel principal: las clases de quien consulta. */
    @GetMapping
    public ResponseEntity<List<ClassResponse>> misClases() {
        return ResponseEntity.ok(schoolClassService.misClases());
    }

    @PostMapping
    public ResponseEntity<ClassDetailResponse> crear(@Valid @RequestBody CreateClassRequest peticion) {
        ClassDetailResponse creada = schoolClassService.crear(peticion);
        return ResponseEntity.created(URI.create("/api/classes/" + creada.id())).body(creada);
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ClassDetailResponse> detalle(@PathVariable UUID classId) {
        return ResponseEntity.ok(schoolClassService.detalle(classId));
    }

    @PutMapping("/{classId}")
    public ResponseEntity<ClassDetailResponse> actualizar(@PathVariable UUID classId,
                                                          @Valid @RequestBody CreateClassRequest peticion) {
        return ResponseEntity.ok(schoolClassService.actualizar(classId, peticion));
    }

    // ----------------------------------------------------------- matrículas

    @GetMapping("/{classId}/members")
    public ResponseEntity<List<MemberResponse>> miembros(@PathVariable UUID classId) {
        return ResponseEntity.ok(schoolClassService.miembros(classId));
    }

    @PostMapping("/{classId}/members")
    public ResponseEntity<MemberResponse> matricular(@PathVariable UUID classId,
                                                     @Valid @RequestBody EnrollRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schoolClassService.matricular(classId, peticion));
    }

    @DeleteMapping("/{classId}/members/{userId}")
    public ResponseEntity<Void> desmatricular(@PathVariable UUID classId, @PathVariable UUID userId) {
        schoolClassService.desmatricular(classId, userId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------- temas

    @GetMapping("/{classId}/topics")
    public ResponseEntity<List<TopicResponse>> temas(@PathVariable UUID classId) {
        return ResponseEntity.ok(schoolClassService.temas(classId));
    }

    @PostMapping("/{classId}/topics")
    public ResponseEntity<TopicResponse> crearTema(@PathVariable UUID classId,
                                                   @Valid @RequestBody CreateTopicRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schoolClassService.crearTema(classId, peticion));
    }

    // ---------------------------------------------------------------- tareas

    /** Trabajo de clase. Lo que se devuelve depende de quién pregunte. */
    @GetMapping("/{classId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> tareas(@PathVariable UUID classId) {
        return ResponseEntity.ok(assignmentService.listar(classId));
    }

    @PostMapping("/{classId}/assignments")
    public ResponseEntity<AssignmentDetailResponse> crearTarea(@PathVariable UUID classId,
                                                               @Valid @RequestBody SaveAssignmentRequest peticion) {
        AssignmentDetailResponse creada = assignmentService.crear(classId, peticion);
        return ResponseEntity.created(URI.create("/api/assignments/" + creada.id())).body(creada);
    }

    // -------------------------------------------------------- calificaciones

    /** Pestaña de calificaciones: las entregas propias en esta clase. */
    @GetMapping("/{classId}/my-submissions")
    public ResponseEntity<List<SubmissionResponse>> misEntregas(@PathVariable UUID classId) {
        return ResponseEntity.ok(submissionService.misEntregasDeClase(classId));
    }

    // ---------------------------------------------------------------- avisos

    /** Muro de la clase: fijados primero, luego lo más reciente. */
    @GetMapping("/{classId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> avisos(@PathVariable UUID classId) {
        return ResponseEntity.ok(announcementService.listar(classId));
    }

    @PostMapping("/{classId}/announcements")
    public ResponseEntity<AnnouncementResponse> publicarAviso(@PathVariable UUID classId,
                                                               @Valid @RequestBody SaveAnnouncementRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(announcementService.crear(classId, peticion));
    }

    // ------------------------------------------------------------ materiales

    @GetMapping("/{classId}/resources")
    public ResponseEntity<List<ResourceResponse>> materiales(@PathVariable UUID classId) {
        return ResponseEntity.ok(resourceService.listar(classId));
    }

    @PostMapping("/{classId}/resources")
    public ResponseEntity<ResourceResponse> anadirMaterial(@PathVariable UUID classId,
                                                            @Valid @RequestBody SaveResourceRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.crear(classId, peticion));
    }

    // -------------------------------------------------------------- exámenes

    /** Exámenes de la clase. Lo que se devuelve depende de quién pregunte. */
    @GetMapping("/{classId}/exams")
    public ResponseEntity<List<ExamResponse>> examenes(@PathVariable UUID classId) {
        return ResponseEntity.ok(examService.listar(classId));
    }

    @PostMapping("/{classId}/exams")
    public ResponseEntity<ExamDetailResponse> crearExamen(@PathVariable UUID classId,
                                                          @Valid @RequestBody SaveExamRequest peticion) {
        ExamDetailResponse creado = examService.crear(classId, peticion);
        return ResponseEntity.created(URI.create("/api/exams/" + creado.id())).body(creado);
    }

    /** Preguntas ya usadas en algún examen de la clase, para reutilizarlas en uno nuevo. Solo profesorado. */
    @GetMapping("/{classId}/exams/question-bank")
    public ResponseEntity<List<BankQuestionResponse>> bancoDePreguntas(@PathVariable UUID classId) {
        return ResponseEntity.ok(examService.listarBanco(classId));
    }
}
