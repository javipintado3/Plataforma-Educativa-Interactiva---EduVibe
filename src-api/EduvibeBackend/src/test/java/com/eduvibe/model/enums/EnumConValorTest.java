package com.eduvibe.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnumConValorTest {

    @Test
    @DisplayName("convierte el valor almacenado en la constante")
    void convierte() {
        assertThat(UserRole.desdeValor("teacher")).isEqualTo(UserRole.TEACHER);
        assertThat(UserStatus.desdeValor("pending")).isEqualTo(UserStatus.PENDING);
        assertThat(EnrollmentRole.desdeValor("student")).isEqualTo(EnrollmentRole.STUDENT);
        assertThat(SubmissionStatus.desdeValor("graded")).isEqualTo(SubmissionStatus.GRADED);
    }

    @Test
    @DisplayName("no distingue mayúsculas")
    void sinDistinguirMayusculas() {
        assertThat(UserRole.desdeValor("ADMIN")).isEqualTo(UserRole.ADMIN);
        assertThat(UserRole.desdeValor("Teacher")).isEqualTo(UserRole.TEACHER);
    }

    @Test
    @DisplayName("un valor desconocido falla diciendo cuáles se admiten")
    void valorDesconocido() {
        assertThatThrownBy(() -> UserRole.desdeValor("jefe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jefe")
                .hasMessageContaining("admin, teacher, student, guardian");
    }

    @Test
    @DisplayName("el valor almacenado va en minúsculas aunque la constante sea mayúscula")
    void elValorVaEnMinusculas() {
        // Es lo que esperan las restricciones CHECK de las columnas
        assertThat(UserRole.ADMIN.getValor()).isEqualTo("admin");
        assertThat(SubmissionStatus.SUBMITTED.getValor()).isEqualTo("submitted");
    }
}
