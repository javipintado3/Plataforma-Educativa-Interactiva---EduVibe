package com.eduvibe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduvibe.dto.submission.EntregaPorCorregirResponse;
import com.eduvibe.model.Assignment;
import com.eduvibe.model.ClassGroup;
import com.eduvibe.model.Organization;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.Submission;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.UserRole;
import com.eduvibe.repository.SubmissionRepository;
import com.eduvibe.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SubmissionService submissionService;

    private final Organization organizacion = new Organization("Centro", null);
    private final SchoolClass clase = clase();
    private final User profesor = usuario("Profe", UserRole.TEACHER);
    private final AuthenticatedUser identidad =
            new AuthenticatedUser(UUID.randomUUID(), "profe@centro.es", "Profe", UserRole.TEACHER, UUID.randomUUID());

    @Test
    @DisplayName("Lista una fila por entrega individual pendiente, con su clase y su tarea")
    void listsIndividualPendingSubmissions() {
        Assignment tarea = tarea("Redacción", false);
        Submission ana = entrega(tarea, usuario("Ana", UserRole.STUDENT), null);
        Submission luis = entrega(tarea, usuario("Luis", UserRole.STUDENT), null);
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(identidad.id())).thenReturn(List.of(ana, luis));

        List<EntregaPorCorregirResponse> resultado = submissionService.porCorregir();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(EntregaPorCorregirResponse::studentName).containsExactly("Ana", "Luis");
        assertThat(resultado.get(0).className()).isEqualTo("1º Bachillerato A");
        assertThat(resultado.get(0).assignmentTitle()).isEqualTo("Redacción");
        assertThat(resultado.get(0).groupName()).isNull();
    }

    @Test
    @DisplayName("En una tarea grupal lista una sola fila por subgrupo, no una por miembro")
    void listsOneRowPerGroupInGroupAssignments() {
        Assignment tarea = tarea("Proyecto en equipo", true);
        ClassGroup grupo = grupo("Equipo 1");
        Submission ana = entrega(tarea, usuario("Ana", UserRole.STUDENT), grupo);
        Submission luis = entrega(tarea, usuario("Luis", UserRole.STUDENT), grupo);
        Submission otroEquipo = entrega(tarea, usuario("Marta", UserRole.STUDENT), grupo("Equipo 2"));
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(identidad.id()))
                .thenReturn(List.of(ana, luis, otroEquipo));

        List<EntregaPorCorregirResponse> resultado = submissionService.porCorregir();

        assertThat(resultado).extracting(EntregaPorCorregirResponse::groupName)
                .containsExactly("Equipo 1", "Equipo 2");
    }

    @Test
    @DisplayName("Sin entregas pendientes devuelve una lista vacía")
    void returnsEmptyWhenNothingPending() {
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(identidad.id())).thenReturn(List.of());

        assertThat(submissionService.porCorregir()).isEmpty();
    }

    private SchoolClass clase() {
        SchoolClass c = new SchoolClass(new Organization("Centro", null), "1º Bachillerato A", "Matemáticas", null, null);
        c.setId(UUID.randomUUID());
        return c;
    }

    private User usuario(String nombre, UserRole rol) {
        User u = new User(organizacion, nombre.toLowerCase() + "@centro.es", nombre, rol);
        u.setId(UUID.randomUUID());
        return u;
    }

    private ClassGroup grupo(String nombre) {
        ClassGroup g = new ClassGroup(clase, nombre);
        g.setId(UUID.randomUUID());
        return g;
    }

    private Assignment tarea(String titulo, boolean grupal) {
        Assignment a = new Assignment(clase, titulo, null, null, 100, profesor);
        a.setId(UUID.randomUUID());
        a.setGroupAssignment(grupal);
        return a;
    }

    private Submission entrega(Assignment tarea, User alumno, ClassGroup grupo) {
        Submission s = new Submission(tarea, alumno);
        s.setId(UUID.randomUUID());
        s.setClassGroup(grupo);
        s.enviar("Trabajo", null, Instant.parse("2026-09-26T10:00:00Z"));
        return s;
    }
}
