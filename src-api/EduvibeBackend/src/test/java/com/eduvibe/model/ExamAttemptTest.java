package com.eduvibe.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.eduvibe.model.enums.UserRole;

class ExamAttemptTest {

    private final Organization centro = new Organization("Centro", null);
    private final User profesor = new User(centro, "profe@centro.es", "Profe", UserRole.TEACHER);
    private final User alumno = new User(centro, "ana@centro.es", "Ana", UserRole.STUDENT);

    private Exam examen(int durationMinutes) {
        SchoolClass clase = new SchoolClass(centro, "1º A", "Matemáticas", "#2563eb", null);
        return new Exam(clase, "Examen 1", null, durationMinutes, null, profesor);
    }

    @Nested
    @DisplayName("Estado del intento")
    class Estados {

        @Test
        @DisplayName("nace sin entregar y sin nota")
        void naceSinEntregar() {
            ExamAttempt intento = new ExamAttempt(examen(30), alumno);

            assertThat(intento.estaEntregado()).isFalse();
            assertThat(intento.getScore()).isNull();
            assertThat(intento.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("al entregar se fijan la fecha y la nota")
        void alEntregarSeFijanFechaYNota() {
            ExamAttempt intento = new ExamAttempt(examen(30), alumno);

            intento.entregar(BigDecimal.valueOf(7));

            assertThat(intento.estaEntregado()).isTrue();
            assertThat(intento.getSubmittedAt()).isNotNull();
            assertThat(intento.getScore()).isEqualByComparingTo("7");
        }
    }

    @Nested
    @DisplayName("Límite de tiempo")
    class LimiteDeTiempo {

        @Test
        @DisplayName("se calcula sumando la duración del examen al inicio del intento")
        void seCalculaDesdeElInicio() {
            ExamAttempt intento = new ExamAttempt(examen(45), alumno);

            Instant limite = intento.limiteDeTiempo();

            assertThat(limite).isEqualTo(intento.getStartedAt().plusSeconds(45 * 60L));
        }
    }

    @Nested
    @DisplayName("Corrección de una respuesta")
    class Correccion {

        @Test
        @DisplayName("una respuesta con la opción correcta marcada es correcta")
        void respuestaConOpcionCorrecta() {
            Exam examen = examen(30);
            ExamQuestion pregunta = new ExamQuestion(examen, "¿2+2?", 1, 0);
            ExamOption correcta = new ExamOption(pregunta, "4", true, 0);
            ExamAttempt intento = new ExamAttempt(examen, alumno);

            ExamAnswer respuesta = new ExamAnswer(intento, pregunta, correcta);

            assertThat(respuesta.esCorrecta()).isTrue();
        }

        @Test
        @DisplayName("una respuesta con una opción incorrecta no es correcta")
        void respuestaConOpcionIncorrecta() {
            Exam examen = examen(30);
            ExamQuestion pregunta = new ExamQuestion(examen, "¿2+2?", 1, 0);
            ExamOption incorrecta = new ExamOption(pregunta, "5", false, 1);
            ExamAttempt intento = new ExamAttempt(examen, alumno);

            ExamAnswer respuesta = new ExamAnswer(intento, pregunta, incorrecta);

            assertThat(respuesta.esCorrecta()).isFalse();
        }

        @Test
        @DisplayName("una pregunta sin responder no es correcta")
        void preguntaSinResponderNoEsCorrecta() {
            Exam examen = examen(30);
            ExamQuestion pregunta = new ExamQuestion(examen, "¿2+2?", 1, 0);
            ExamAttempt intento = new ExamAttempt(examen, alumno);

            ExamAnswer respuesta = new ExamAnswer(intento, pregunta, null);

            assertThat(respuesta.esCorrecta()).isFalse();
        }
    }
}
