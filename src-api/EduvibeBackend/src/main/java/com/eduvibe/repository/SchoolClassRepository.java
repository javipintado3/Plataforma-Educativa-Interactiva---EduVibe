package com.eduvibe.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduvibe.model.SchoolClass;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {

    /** Todas las clases del centro: es la vista de un administrador. */
    List<SchoolClass> findByOrganizationIdOrderByNameAscSubjectAsc(UUID organizationId);

    /** Las clases en las que participa una persona, sea profesor o alumno. */
    @Query("""
            SELECT e.schoolClass FROM Enrollment e
            WHERE e.user.id = :userId
            ORDER BY e.schoolClass.name ASC, e.schoolClass.subject ASC
            """)
    List<SchoolClass> findDeUsuario(@Param("userId") UUID userId);

    /**
     * Próxima fecha de entrega de cada clase, para la línea de estado de las
     * tarjetas del panel.
     *
     * Se resuelve en una sola consulta agrupada en lugar de preguntar clase por
     * clase, que sería el problema de las N+1 consultas en su forma más clásica.
     */
    @Query("""
            SELECT a.schoolClass.id AS classId, MIN(a.dueDate) AS proximaEntrega
            FROM Assignment a
            WHERE a.schoolClass.id IN :classIds
              AND a.dueDate IS NOT NULL
              AND a.dueDate > CURRENT_TIMESTAMP
            GROUP BY a.schoolClass.id
            """)
    List<ProximaEntrega> findProximasEntregas(@Param("classIds") List<UUID> classIds);

    /** Proyección del resultado de la consulta anterior. */
    interface ProximaEntrega {
        UUID getClassId();

        Instant getProximaEntrega();
    }
}
