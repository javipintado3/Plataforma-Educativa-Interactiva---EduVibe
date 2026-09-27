package com.eduvibe.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.analytics.AssignmentAnalyticsResponse;
import com.eduvibe.dto.analytics.ClassAnalyticsResponse;
import com.eduvibe.dto.analytics.GradePointResponse;
import com.eduvibe.dto.analytics.StudentAnalyticsResponse;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.Enrollment;
import com.eduvibe.model.Grade;
import com.eduvibe.model.Submission;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.EnrollmentRole;
import com.eduvibe.repository.AssignmentRepository;
import com.eduvibe.repository.EnrollmentRepository;
import com.eduvibe.repository.GradeRepository;
import com.eduvibe.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Panel de analítica del profesorado: quién ha entregado, quién no, y quién
 * acumula tareas vencidas sin entregar.
 *
 * Todo se calcula al vuelo a partir de lo que ya hay (matrículas, tareas,
 * entregas): no se guarda ningún dato nuevo, así que no hace falta migración.
 * El precio es recalcular en cada consulta, pero una clase tiene decenas de
 * alumnos y tareas, no miles, así que no compensa la complejidad de cachear.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    /** A partir de cuántas tareas ya vencidas sin entregar se marca a alguien en riesgo. */
    private static final int UMBRAL_RIESGO = 2;

    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final ClassAccessService acceso;

    @Transactional(readOnly = true)
    public ClassAnalyticsResponse analiticaDe(UUID classId) {
        acceso.exigirEditable(classId);

        List<Enrollment> alumnado = enrollmentRepository
                .findBySchoolClassIdAndRoleInClassOrderByUserNameAsc(classId, EnrollmentRole.STUDENT);
        List<Assignment> tareas = assignmentRepository.findDeClase(classId);
        List<Submission> entregas = submissionRepository.findDeClase(classId);

        Map<UUID, Grade> notaPorEntrega = notasDe(entregas);

        // studentId -> assignmentId -> su entrega, solo lo que ya no es borrador:
        // un borrador todavía no cuenta como "entregado".
        Map<UUID, Map<UUID, Submission>> entregadoPor = new HashMap<>();
        for (Submission entrega : entregas) {
            if (entrega.esBorrador()) {
                continue;
            }
            entregadoPor.computeIfAbsent(entrega.getStudent().getId(), k -> new HashMap<>())
                    .put(entrega.getAssignment().getId(), entrega);
        }

        List<Assignment> vencidas = tareas.stream().filter(Assignment::haVencido).toList();

        List<StudentAnalyticsResponse> alumnos = new ArrayList<>();
        long enRiesgo = 0;
        for (Enrollment matricula : alumnado) {
            StudentAnalyticsResponse fila = analiticaDeAlumno(matricula.getUser(), tareas, vencidas,
                    entregadoPor.getOrDefault(matricula.getUser().getId(), Map.of()), notaPorEntrega);
            alumnos.add(fila);
            if (fila.atRisk()) {
                enRiesgo++;
            }
        }

        List<AssignmentAnalyticsResponse> tareasResp = tareas.stream()
                .map(tarea -> analiticaDeTarea(tarea, entregadoPor, alumnado.size()))
                .toList();

        int porcentajeGlobal = alumnos.isEmpty() ? 100
                : (int) Math.round(alumnos.stream().mapToInt(StudentAnalyticsResponse::completionPercent)
                        .average().orElse(100));

        return new ClassAnalyticsResponse(alumnado.size(), porcentajeGlobal, enRiesgo, tareasResp, alumnos);
    }

    private StudentAnalyticsResponse analiticaDeAlumno(User alumno, List<Assignment> tareas,
                                                        List<Assignment> vencidas,
                                                        Map<UUID, Submission> propias,
                                                        Map<UUID, Grade> notaPorEntrega) {
        long entregadas = tareas.stream().filter(t -> propias.containsKey(t.getId())).count();
        long pendientesVencidas = vencidas.stream().filter(t -> !propias.containsKey(t.getId())).count();
        int total = tareas.size();
        int porcentaje = total == 0 ? 100 : (int) Math.round(entregadas * 100.0 / total);

        BigDecimal sumaPonderada = BigDecimal.ZERO;
        BigDecimal sumaPesos = BigDecimal.ZERO;
        List<GradePointResponse> puntos = new ArrayList<>();

        for (Assignment tarea : tareas) {
            Submission entrega = propias.get(tarea.getId());
            Grade nota = entrega == null ? null : notaPorEntrega.get(entrega.getId());
            if (nota == null) {
                continue;
            }
            BigDecimal porcentajeNota = porcentajeDe(nota.getScore(), tarea.getPoints());
            sumaPonderada = sumaPonderada.add(porcentajeNota.multiply(tarea.getWeight()));
            sumaPesos = sumaPesos.add(tarea.getWeight());
            puntos.add(new GradePointResponse(tarea.getTitle(), nota.getGradedAt(), porcentajeNota));
        }

        BigDecimal media = sumaPesos.signum() == 0 ? null
                : sumaPonderada.divide(sumaPesos, 1, RoundingMode.HALF_UP);

        return new StudentAnalyticsResponse(alumno.getId(), alumno.getName(), entregadas, total - entregadas,
                total, porcentaje, media, pendientesVencidas >= UMBRAL_RIESGO, puntos);
    }

    private AssignmentAnalyticsResponse analiticaDeTarea(Assignment tarea,
                                                          Map<UUID, Map<UUID, Submission>> entregadoPor,
                                                          long alumnadoDeLaClase) {
        long entregadas = entregadoPor.values().stream()
                .filter(porAlumno -> porAlumno.containsKey(tarea.getId()))
                .count();
        long pendientes = alumnadoDeLaClase - entregadas;
        int porcentaje = alumnadoDeLaClase == 0 ? 100 : (int) Math.round(entregadas * 100.0 / alumnadoDeLaClase);

        return new AssignmentAnalyticsResponse(tarea.getId(), tarea.getTitle(), tarea.getDueDate(),
                entregadas, pendientes, porcentaje);
    }

    private Map<UUID, Grade> notasDe(List<Submission> entregas) {
        List<UUID> idsCalificadas = entregas.stream()
                .filter(Submission::estaCalificada)
                .map(Submission::getId)
                .toList();

        if (idsCalificadas.isEmpty()) {
            return Map.of();
        }
        return gradeRepository.findDeEntregas(idsCalificadas).stream()
                .collect(Collectors.toMap(nota -> nota.getSubmission().getId(), nota -> nota));
    }

    private BigDecimal porcentajeDe(BigDecimal score, int puntosDeLaTarea) {
        return score.divide(BigDecimal.valueOf(puntosDeLaTarea), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
