package com.eduvibe.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.enums.EnrollmentRole;
import com.eduvibe.repository.EnrollmentRepository;
import com.eduvibe.repository.SchoolClassRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Quién puede ver y quién puede tocar cada clase.
 *
 * Toda la fase de clases, tareas y entregas hace la misma comprobación una y
 * otra vez, así que vive en un único sitio en lugar de repetida en cada
 * servicio. Si mañana cambia la regla —por ejemplo, que un tutor pueda ver las
 * clases de su hijo— se cambia aquí y queda cambiada en todas partes.
 *
 * Las reglas son:
 *
 *  - Ver una clase: estar matriculado en ella, o ser administrador del centro.
 *  - Editarla y crear contenido: ser profesor de esa clase, o administrador.
 *  - Entregar: estar matriculado como alumno.
 *
 * Nótese que ser TEACHER en la organización no basta para editar una clase: hay
 * que impartirla. Un profesor no puede poner tareas en la clase de otro.
 */
@Service
@RequiredArgsConstructor
public class ClassAccessService {

    private final SchoolClassRepository schoolClassRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuthService authService;

    /**
     * Devuelve la clase si quien pregunta puede verla.
     *
     * Una clase de otra organización se trata como inexistente y no como
     * prohibida: responder 403 confirmaría que ese identificador existe.
     */
    @Transactional(readOnly = true)
    public SchoolClass exigirVisible(UUID classId) {
        AuthenticatedUser usuario = authService.identidadActual();
        SchoolClass clase = buscarEnMiOrganizacion(classId, usuario);

        if (usuario.esAdmin() || esMiembroDe(classId, usuario.id())) {
            return clase;
        }
        throw new AccessDeniedException("No participas en esta clase");
    }

    /** Devuelve la clase si quien pregunta puede modificarla o añadirle contenido. */
    @Transactional(readOnly = true)
    public SchoolClass exigirEditable(UUID classId) {
        AuthenticatedUser usuario = authService.identidadActual();
        SchoolClass clase = buscarEnMiOrganizacion(classId, usuario);

        if (usuario.esAdmin() || esProfesorDe(classId, usuario.id())) {
            return clase;
        }
        throw new AccessDeniedException("Solo el profesorado de esta clase puede hacer esto");
    }

    /** Comprueba que quien pregunta es alumno de la clase. */
    @Transactional(readOnly = true)
    public void exigirSerAlumnoDe(UUID classId) {
        AuthenticatedUser usuario = authService.identidadActual();
        buscarEnMiOrganizacion(classId, usuario);

        boolean esAlumno = enrollmentRepository.existsBySchoolClassIdAndUserIdAndRoleInClass(
                classId, usuario.id(), EnrollmentRole.STUDENT);

        if (!esAlumno) {
            throw new AccessDeniedException("Solo el alumnado matriculado puede entregar en esta clase");
        }
    }

    public boolean esProfesorDe(UUID classId, UUID userId) {
        return enrollmentRepository.existsBySchoolClassIdAndUserIdAndRoleInClass(
                classId, userId, EnrollmentRole.TEACHER);
    }

    public boolean esMiembroDe(UUID classId, UUID userId) {
        return enrollmentRepository.existsBySchoolClassIdAndUserId(classId, userId);
    }

    /**
     * Si quien pregunta puede corregir en esa clase: profesorado de la clase o
     * administración. Se expone porque los DTO de entrega lo necesitan para
     * decidir qué información incluir.
     */
    public boolean puedeCalificarEn(UUID classId) {
        AuthenticatedUser usuario = authService.identidadActual();
        return usuario.esAdmin() || esProfesorDe(classId, usuario.id());
    }

    private SchoolClass buscarEnMiOrganizacion(UUID classId, AuthenticatedUser usuario) {
        return schoolClassRepository.findById(classId)
                .filter(c -> c.getOrganization().getId().equals(usuario.organizationId()))
                .orElseThrow(() -> NotFoundException.de("Clase", classId));
    }
}
