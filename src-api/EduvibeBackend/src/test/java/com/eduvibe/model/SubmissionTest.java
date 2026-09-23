package com.eduvibe.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.eduvibe.model.enums.SubmissionStatus;
import com.eduvibe.model.enums.UserRole;

class SubmissionTest {

    private final Organization centro = new Organization("Centro", null);
    private final User profesor = new User(centro, "profe@centro.es", "Profe", UserRole.TEACHER);
    private final User alumno = new User(centro, "ana@centro.es", "Ana", UserRole.STUDENT);

    private Assignment tareaConPlazo(Duration desdeAhora) {
        SchoolClass clase = new SchoolClass(centro, "1º A", "Matemáticas", "#2563eb");
        return new Assignment(clase, "Ejercicios", null, Instant.now().plus(desdeAhora), 100, profesor);
    }

    private Assignment tareaSinPlazo() {
        SchoolClass clase = new SchoolClass(centro, "1º A", "Matemáticas", "#2563eb");
        return new Assignment(clase, "Ejercicios", null, null, 100, profesor);
    }

    @Nested
    @DisplayName("Estados de la entrega")
    class Estados {

        @Test
        @DisplayName("nace como borrador y sin fecha de envío")
        void naceBorrador() {
            Submission entrega = new Submission(tareaSinPlazo(), alumno);

            assertThat(entrega.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
            assertThat(entrega.getSubmittedAt()).isNull();
            assertThat(entrega.esBorrador()).isTrue();
        }

        @Test
        @DisplayName("al enviar se fija la fecha de envío")
        void alEnviarSeFijaLaFecha() {
            Submission entrega = new Submission(tareaSinPlazo(), alumno);

            entrega.enviar("Mi respuesta", null);

            assertThat(entrega.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
            assertThat(entrega.getSubmittedAt()).isNotNull();
        }

        @Test
        @DisplayName("volver a borrador borra la fecha de envío")
        void volverABorradorLimpiaLaFecha() {
            Submission entrega = new Submission(tareaSinPlazo(), alumno);
            entrega.enviar("Algo", null);

            entrega.guardarBorrador("Sigo trabajando", null);

            // La base de datos exige fecha de envío en todo lo que no sea
            // borrador, y la inversa: un borrador no puede tenerla
            assertThat(entrega.getSubmittedAt()).isNull();
            assertThat(entrega.esBorrador()).isTrue();
        }
    }

    @Nested
    @DisplayName("Entrega fuera de plazo")
    class FueraDePlazo {

        @Test
        @DisplayName("un borrador nunca consta como tarde: todavía no se ha entregado")
        void borradorNoEsTarde() {
            Submission entrega = new Submission(tareaConPlazo(Duration.ofDays(-5)), alumno);

            assertThat(entrega.entregadaTarde()).isFalse();
        }

        @Test
        @DisplayName("entregar dentro de plazo no es tarde")
        void dentroDePlazo() {
            Submission entrega = new Submission(tareaConPlazo(Duration.ofDays(3)), alumno);

            entrega.enviar("A tiempo", null);

            assertThat(entrega.entregadaTarde()).isFalse();
        }

        @Test
        @DisplayName("entregar pasado el plazo sí es tarde")
        void pasadoElPlazo() {
            Submission entrega = new Submission(tareaConPlazo(Duration.ofDays(-1)), alumno);

            entrega.enviar("Se me pasó", null);

            assertThat(entrega.entregadaTarde()).isTrue();
        }

        @Test
        @DisplayName("una tarea sin plazo no se entrega tarde nunca")
        void sinPlazoNuncaEsTarde() {
            Submission entrega = new Submission(tareaSinPlazo(), alumno);

            entrega.enviar("Cuando sea", null);

            assertThat(entrega.entregadaTarde()).isFalse();
        }

        @Test
        @DisplayName("sigue constando como tarde después de calificarla")
        void calificarNoBorraElRetraso() {
            Submission entrega = new Submission(tareaConPlazo(Duration.ofDays(-2)), alumno);
            entrega.enviar("Tarde", null);

            entrega.marcarComoCalificada();

            // Este es justo el motivo de que 'late' no sea un estado más: si lo
            // fuese, calificar la entrega borraría el dato de que llegó tarde
            assertThat(entrega.estaCalificada()).isTrue();
            assertThat(entrega.entregadaTarde()).isTrue();
        }
    }
}
