package com.eduvibe.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Enrollment;
import com.eduvibe.model.enums.EnrollmentRole;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findBySchoolClassIdAndUserId(UUID classId, UUID userId);

    boolean existsBySchoolClassIdAndUserId(UUID classId, UUID userId);

    boolean existsBySchoolClassIdAndUserIdAndRoleInClass(UUID classId, UUID userId, EnrollmentRole rol);

    /**
     * Personas de una clase. Se trae el usuario en la misma consulta porque
     * siempre se va a necesitar su nombre y su email: sin esto, pintar la lista
     * dispararía una consulta por fila.
     */
    @EntityGraph(attributePaths = "user")
    List<Enrollment> findBySchoolClassIdOrderByRoleInClassAscUserNameAsc(UUID classId);

    @EntityGraph(attributePaths = "user")
    List<Enrollment> findBySchoolClassIdAndRoleInClassOrderByUserNameAsc(UUID classId, EnrollmentRole rol);

    long countBySchoolClassIdAndRoleInClass(UUID classId, EnrollmentRole rol);

    /**
     * Matriculaciones de varias clases en una sola consulta. Es lo que permite
     * pintar el panel de clases sin lanzar una consulta por tarjeta.
     */
    @EntityGraph(attributePaths = "user")
    List<Enrollment> findBySchoolClassIdIn(List<UUID> classIds);
}
