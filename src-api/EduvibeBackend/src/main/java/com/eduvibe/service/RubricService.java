package com.eduvibe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.rubric.RubricResponse;
import com.eduvibe.dto.rubric.SaveRubricRequest;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.Rubric;
import com.eduvibe.model.RubricCriterion;
import com.eduvibe.repository.AssignmentRepository;
import com.eduvibe.repository.RubricCriterionRepository;
import com.eduvibe.repository.RubricRepository;

import lombok.RequiredArgsConstructor;

/**
 * Rúbrica de una tarea: sus criterios y la puntuación máxima de cada uno.
 *
 * Se guarda siempre entera (ver {@link #guardar}): no hay un PATCH criterio a
 * criterio, igual que un examen no se edita pregunta a pregunta desde fuera.
 * Cambiar los criterios de una tarea que ya tiene entregas corregidas con la
 * rúbrica anterior es decisión de quien la edita, no algo que este servicio
 * intente impedir.
 */
@Service
@RequiredArgsConstructor
public class RubricService {

    private final RubricRepository rubricRepository;
    private final RubricCriterionRepository rubricCriterionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClassAccessService acceso;

    @Transactional(readOnly = true)
    public RubricResponse obtenerSiExiste(UUID assignmentId) {
        return rubricRepository.findByAssignmentId(assignmentId)
                .map(rubrica -> RubricResponse.de(rubrica,
                        rubricCriterionRepository.findByRubricIdOrderBySortOrderAsc(rubrica.getId())))
                .orElse(null);
    }

    /** Crea la rúbrica de la tarea, o sustituye por completo la que ya hubiera. */
    @Transactional
    public RubricResponse guardar(UUID assignmentId, SaveRubricRequest peticion) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        acceso.exigirEditable(tarea.getSchoolClass().getId());

        Rubric rubrica = rubricRepository.findByAssignmentId(assignmentId)
                .orElseGet(() -> new Rubric(tarea));
        rubricRepository.saveAndFlush(rubrica);

        // Se reemplazan todos los criterios: es más simple y seguro que calcular
        // un diff, y una rúbrica no tiene tantos criterios como para que borrar
        // y recrear sea un problema de rendimiento.
        rubricCriterionRepository.deleteAll(
                rubricCriterionRepository.findByRubricIdOrderBySortOrderAsc(rubrica.getId()));

        List<RubricCriterion> criterios = new ArrayList<>();
        int orden = 0;
        for (SaveRubricRequest.CriterionInput entrada : peticion.criteria()) {
            criterios.add(new RubricCriterion(rubrica, entrada.description().trim(), entrada.maxPoints(), orden++));
        }
        rubricCriterionRepository.saveAll(criterios);

        return RubricResponse.de(rubrica, criterios);
    }

    @Transactional
    public void eliminar(UUID assignmentId) {
        Assignment tarea = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> NotFoundException.de("Tarea", assignmentId));

        acceso.exigirEditable(tarea.getSchoolClass().getId());

        rubricRepository.findByAssignmentId(assignmentId).ifPresent(rubricRepository::delete);
    }

    /** Para el servicio de corrección: la rúbrica de la tarea si la tiene, sin exigir permisos aparte. */
    @Transactional(readOnly = true)
    public Optional<Rubric> buscarDe(UUID assignmentId) {
        return rubricRepository.findByAssignmentId(assignmentId);
    }
}
