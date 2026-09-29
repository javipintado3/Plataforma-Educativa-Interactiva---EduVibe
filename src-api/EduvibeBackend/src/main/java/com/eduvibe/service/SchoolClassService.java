package com.eduvibe.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.schoolclass.ClassDetailResponse;
import com.eduvibe.dto.schoolclass.ClassResponse;
import com.eduvibe.dto.schoolclass.CreateClassRequest;
import com.eduvibe.dto.schoolclass.CreateTopicRequest;
import com.eduvibe.dto.schoolclass.EnrollRequest;
import com.eduvibe.dto.schoolclass.MemberResponse;
import com.eduvibe.dto.schoolclass.TopicResponse;
import com.eduvibe.exception.ConflictException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Enrollment;
import com.eduvibe.model.Organization;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.Topic;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.ClassViewMode;
import com.eduvibe.model.enums.EnrollmentRole;
import com.eduvibe.repository.EnrollmentRepository;
import com.eduvibe.repository.ExamAttemptRepository;
import com.eduvibe.repository.OrganizationRepository;
import com.eduvibe.repository.SchoolClassRepository;
import com.eduvibe.repository.SubmissionRepository;
import com.eduvibe.repository.TopicRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.eduvibe.dto.common.PageResponse;

/**
 * Clases, matriculaciones y temas.
 */
@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private static final String ROL_ADMINISTRACION = "admin";

    private final SchoolClassRepository schoolClassRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final SubmissionRepository submissionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;

    @Transactional
    public ClassDetailResponse crear(CreateClassRequest peticion) {
        AuthenticatedUser admin = authService.identidadActual();

        Organization organizacion = organizationRepository.findById(admin.organizationId())
                .orElseThrow(() -> NotFoundException.de("Organización", admin.organizationId()));

        SchoolClass clase = new SchoolClass(organizacion, peticion.name().trim(),
                normalizar(peticion.subject()), peticion.color(), normalizar(peticion.imageUrl()));
        clase.setViewMode(modoDeVista(peticion.viewMode()));
        schoolClassRepository.saveAndFlush(clase);

        return ClassDetailResponse.de(clase, ROL_ADMINISTRACION, true, List.of(), 0, List.of());
    }

    /**
     * Las clases que ve quien consulta: las suyas, o todas las del centro si es
     * administrador.
     *
     * Lo que necesita cada tarjeta —papel, profesorado y próxima entrega— se
     * resuelve con dos consultas para el conjunto entero, no con dos por clase.
     * Con veinte clases, la diferencia es entre tres consultas y cuarenta y una.
     */
    @Transactional(readOnly = true)
    public PageResponse<ClassResponse> misClases(String busqueda, Pageable pageable) {
        AuthenticatedUser usuario = authService.identidadActual();
        String patron = Paginacion.patronDeBusqueda(busqueda);
        Pageable pagina = Paginacion.sinOrden(pageable);

        Page<SchoolClass> resultado = usuario.esAdmin()
                ? schoolClassRepository.buscarDeCentro(usuario.organizationId(), patron, pagina)
                : schoolClassRepository.buscarDeUsuario(usuario.id(), patron, pagina);
        List<SchoolClass> clases = resultado.getContent();

        if (clases.isEmpty()) {
            return PageResponse.de(resultado, List.<ClassResponse>of());
        }

        List<UUID> ids = clases.stream().map(SchoolClass::getId).toList();

        // Consulta 1: la próxima entrega de cada clase
        Map<UUID, Instant> proximas = new HashMap<>();
        for (var fila : schoolClassRepository.findProximasEntregas(ids)) {
            proximas.put(fila.getClassId(), fila.getProximaEntrega());
        }

        // Consulta 2: todas las matriculaciones de esas clases, de las que salen
        // tanto el profesorado como el alumnado de cada una y el papel de quien consulta
        Map<UUID, List<String>> profesoradoPorClase = new HashMap<>();
        Map<UUID, Integer> alumnadoPorClase = new HashMap<>();
        Map<UUID, String> miRolPorClase = new HashMap<>();

        for (Enrollment matricula : enrollmentRepository.findBySchoolClassIdIn(ids)) {
            UUID claseId = matricula.getSchoolClass().getId();

            if (matricula.esProfesor()) {
                profesoradoPorClase
                        .computeIfAbsent(claseId, k -> new ArrayList<>())
                        .add(matricula.getUser().getName());
            } else {
                alumnadoPorClase.merge(claseId, 1, Integer::sum);
            }
            if (matricula.getUser().getId().equals(usuario.id())) {
                miRolPorClase.put(claseId, matricula.getRoleInClass().getValor());
            }
        }

        String rolSiNoEstaMatriculado = usuario.esAdmin() ? ROL_ADMINISTRACION : null;

        return PageResponse.de(resultado, clases.stream()
                .map(clase -> ClassResponse.de(
                        clase,
                        miRolPorClase.getOrDefault(clase.getId(), rolSiNoEstaMatriculado),
                        proximas.get(clase.getId()),
                        profesoradoPorClase.getOrDefault(clase.getId(), List.of()),
                        alumnadoPorClase.getOrDefault(clase.getId(), 0)))
                .toList());
    }

    /**
     * Borra la clase entera y todo lo que cuelga de ella (matriculaciones,
     * temas, tareas, exámenes, avisos, materiales, foros...).
     *
     * Solo administración puede hacerlo, y solo si nadie ha entregado nada
     * todavía: si ya hay entregas o intentos de examen, borrar destruiría
     * histórico académico real, y lo correcto ahí es archivar, no borrar
     * (archivar queda fuera del alcance actual).
     */
    @Transactional
    public void eliminar(UUID classId) {
        SchoolClass clase = acceso.exigirSerAdmin(classId);

        if (submissionRepository.existsByAssignmentSchoolClassId(classId)
                || examAttemptRepository.existsByExamSchoolClassId(classId)) {
            throw new ConflictException(
                    "No se puede borrar: ya hay entregas o exámenes hechos en esta clase");
        }

        schoolClassRepository.delete(clase);
    }

    @Transactional(readOnly = true)
    public ClassDetailResponse detalle(UUID classId) {
        SchoolClass clase = acceso.exigirVisible(classId);
        AuthenticatedUser usuario = authService.identidadActual();

        List<MemberResponse> profesorado = enrollmentRepository
                .findBySchoolClassIdAndRoleInClassOrderByUserNameAsc(classId, EnrollmentRole.TEACHER)
                .stream().map(MemberResponse::de).toList();

        long alumnado = enrollmentRepository.countBySchoolClassIdAndRoleInClass(classId, EnrollmentRole.STUDENT);

        List<TopicResponse> temas = topicRepository
                .findBySchoolClassIdOrderBySortOrderAscTitleAsc(classId)
                .stream().map(TopicResponse::de).toList();

        return ClassDetailResponse.de(clase, miRolEn(classId, usuario),
                acceso.puedeCalificarEn(classId), profesorado, alumnado, temas);
    }

    @Transactional
    public ClassDetailResponse actualizar(UUID classId, CreateClassRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);

        clase.setName(peticion.name().trim());
        clase.setSubject(normalizar(peticion.subject()));
        clase.setColor(peticion.color());
        clase.setImageUrl(normalizar(peticion.imageUrl()));
        if (peticion.viewMode() != null) {
            clase.setViewMode(ClassViewMode.desdeValor(peticion.viewMode()));
        }
        schoolClassRepository.save(clase);

        return detalle(classId);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> miembros(UUID classId) {
        acceso.exigirVisible(classId);

        return enrollmentRepository.findBySchoolClassIdOrderByRoleInClassAscUserNameAsc(classId)
                .stream().map(MemberResponse::de).toList();
    }

    /**
     * Matricula a una persona en una clase.
     *
     * Solo la administración matricula, y solo a personas de su propio centro.
     */
    @Transactional
    public MemberResponse matricular(UUID classId, EnrollRequest peticion) {
        AuthenticatedUser admin = authService.identidadActual();
        SchoolClass clase = acceso.exigirEditable(classId);

        User usuario = userRepository.findById(peticion.userId())
                .filter(u -> u.getOrganization().getId().equals(admin.organizationId()))
                .orElseThrow(() -> NotFoundException.de("Usuario", peticion.userId()));

        if (enrollmentRepository.existsBySchoolClassIdAndUserId(classId, usuario.getId())) {
            throw new ConflictException(usuario.getName() + " ya está en esta clase");
        }

        Enrollment matricula = new Enrollment(clase, usuario,
                EnrollmentRole.desdeValor(peticion.roleInClass()));
        enrollmentRepository.saveAndFlush(matricula);

        return MemberResponse.de(matricula);
    }

    @Transactional
    public void desmatricular(UUID classId, UUID userId) {
        acceso.exigirEditable(classId);

        Enrollment matricula = enrollmentRepository.findBySchoolClassIdAndUserId(classId, userId)
                .orElseThrow(() -> new NotFoundException("Esa persona no está matriculada en la clase"));

        // Quitar al último profesor dejaría la clase sin nadie que pueda
        // gestionarla, salvo la administración
        if (matricula.esProfesor()
                && enrollmentRepository.countBySchoolClassIdAndRoleInClass(classId, EnrollmentRole.TEACHER) <= 1) {
            throw new ConflictException("La clase se quedaría sin profesorado");
        }

        enrollmentRepository.delete(matricula);
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> temas(UUID classId) {
        acceso.exigirVisible(classId);

        return topicRepository.findBySchoolClassIdOrderBySortOrderAscTitleAsc(classId)
                .stream().map(TopicResponse::de).toList();
    }

    @Transactional
    public TopicResponse crearTema(UUID classId, CreateTopicRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);

        int orden = peticion.sortOrder() != null
                ? peticion.sortOrder()
                : topicRepository.findBySchoolClassIdOrderBySortOrderAscTitleAsc(classId).size() + 1;

        Topic tema = new Topic(clase, peticion.title().trim(), orden);
        topicRepository.saveAndFlush(tema);

        return TopicResponse.de(tema);
    }

    /** Solo renombra: el orden se conserva salvo que la petición traiga uno nuevo explícito. */
    @Transactional
    public TopicResponse actualizarTema(UUID classId, UUID topicId, CreateTopicRequest peticion) {
        acceso.exigirEditable(classId);

        Topic tema = topicRepository.findByIdAndSchoolClassId(topicId, classId)
                .orElseThrow(() -> NotFoundException.de("Tema", topicId));

        tema.setTitle(peticion.title().trim());
        if (peticion.sortOrder() != null) {
            tema.setSortOrder(peticion.sortOrder());
        }
        topicRepository.save(tema);

        return TopicResponse.de(tema);
    }

    /** Papel de la persona en la clase, o "admin" si solo la ve por ser administración. */
    private String miRolEn(UUID classId, AuthenticatedUser usuario) {
        return enrollmentRepository.findBySchoolClassIdAndUserId(classId, usuario.id())
                .map(e -> e.getRoleInClass().getValor())
                .orElse(usuario.esAdmin() ? ROL_ADMINISTRACION : null);
    }

    private List<String> nombresDelProfesorado(UUID classId) {
        return enrollmentRepository
                .findBySchoolClassIdAndRoleInClassOrderByUserNameAsc(classId, EnrollmentRole.TEACHER)
                .stream().map(e -> e.getUser().getName()).toList();
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private ClassViewMode modoDeVista(String viewMode) {
        return viewMode != null ? ClassViewMode.desdeValor(viewMode) : ClassViewMode.STRUCTURED;
    }
}
