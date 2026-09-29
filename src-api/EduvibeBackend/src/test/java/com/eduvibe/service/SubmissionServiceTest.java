package com.eduvibe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.eduvibe.dto.common.PageResponse;
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
    @DisplayName("Devuelve la página pedida con la clase y la tarea de cada entrega pendiente")
    void returnsRequestedPageWithClassAndAssignment() {
        Assignment tarea = tarea("Redacción");
        Submission ana = entrega(tarea, usuario("Ana", UserRole.STUDENT), null);
        Submission luis = entrega(tarea, usuario("Luis", UserRole.STUDENT), null);
        Pageable pedida = PageRequest.of(0, 10);
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(any(), any()))
                .thenReturn(new PageImpl<>(List.of(ana, luis), pedida, 2));

        PageResponse<EntregaPorCorregirResponse> resultado = submissionService.porCorregir(pedida);

        assertThat(resultado.contenido()).extracting(EntregaPorCorregirResponse::studentName)
                .containsExactly("Ana", "Luis");
        assertThat(resultado.contenido().get(0).className()).isEqualTo("1º Bachillerato A");
        assertThat(resultado.contenido().get(0).assignmentTitle()).isEqualTo("Redacción");
        assertThat(resultado.contenido().get(0).groupName()).isNull();
    }

    @Test
    @DisplayName("Informa del total de entregas y de páginas, no solo de las de la página actual")
    void reportsTotalsOfTheWholeQueue() {
        Assignment tarea = tarea("Redacción");
        Submission ana = entrega(tarea, usuario("Ana", UserRole.STUDENT), null);
        Pageable pedida = PageRequest.of(1, 10);
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(any(), any()))
                .thenReturn(new PageImpl<>(List.of(ana), pedida, 11));

        PageResponse<EntregaPorCorregirResponse> resultado = submissionService.porCorregir(pedida);

        assertThat(resultado.pagina()).isEqualTo(1);
        assertThat(resultado.tamano()).isEqualTo(10);
        assertThat(resultado.totalElementos()).isEqualTo(11);
        assertThat(resultado.totalPaginas()).isEqualTo(2);
        assertThat(resultado.ultima()).isTrue();
    }

    @Test
    @DisplayName("Una entrega grupal se muestra con el nombre de su subgrupo")
    void showsTheGroupNameInGroupSubmissions() {
        Assignment tarea = tarea("Proyecto en equipo");
        Submission delEquipo = entrega(tarea, usuario("Ana", UserRole.STUDENT), grupo("Equipo 1"));
        Pageable pedida = PageRequest.of(0, 10);
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(any(), any()))
                .thenReturn(new PageImpl<>(List.of(delEquipo), pedida, 1));

        PageResponse<EntregaPorCorregirResponse> resultado = submissionService.porCorregir(pedida);

        assertThat(resultado.contenido()).extracting(EntregaPorCorregirResponse::groupName)
                .containsExactly("Equipo 1");
    }

    @Test
    @DisplayName("Sin entregas pendientes devuelve una página vacía")
    void returnsEmptyPageWhenNothingPending() {
        Pageable pedida = PageRequest.of(0, 10);
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), pedida, 0));

        PageResponse<EntregaPorCorregirResponse> resultado = submissionService.porCorregir(pedida);

        assertThat(resultado.contenido()).isEmpty();
        assertThat(resultado.totalElementos()).isZero();
    }

    @Test
    @DisplayName("Ignora el orden que pida el cliente: el de la cola lo fija la consulta")
    void ignoresClientProvidedSort() {
        Pageable conOrden = PageRequest.of(2, 5, Sort.by("propiedadQueNoExiste"));
        when(authService.identidadActual()).thenReturn(identidad);
        when(submissionRepository.findPorCorregirDeProfesor(any(), any()))
                .thenReturn(Page.empty());

        submissionService.porCorregir(conOrden);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(submissionRepository).findPorCorregirDeProfesor(any(), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().isUnsorted()).isTrue();
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

    private Assignment tarea(String titulo) {
        Assignment a = new Assignment(clase, titulo, null, null, 100, profesor);
        a.setId(UUID.randomUUID());
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
