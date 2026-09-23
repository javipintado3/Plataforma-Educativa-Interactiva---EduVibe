package com.eduvibe.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.eduvibe.model.enums.UserRole;

class InvitationTest {

    private Invitation invitacionQueCaducaEn(Duration plazo) {
        User usuario = new User(new Organization("Centro", null), "ana@centro.es", "Ana", UserRole.STUDENT);
        return new Invitation(usuario, "hash", Instant.now().plus(plazo));
    }

    @Test
    @DisplayName("una invitación recién emitida sirve")
    void reciénEmitidaSirve() {
        assertThat(invitacionQueCaducaEn(Duration.ofHours(48)).esUtilizable()).isTrue();
    }

    @Test
    @DisplayName("una invitación caducada no sirve")
    void caducadaNoSirve() {
        Invitation invitacion = invitacionQueCaducaEn(Duration.ofHours(-1));

        assertThat(invitacion.haExpirado()).isTrue();
        assertThat(invitacion.esUtilizable()).isFalse();
    }

    @Test
    @DisplayName("una invitación solo sirve una vez")
    void unSoloUso() {
        Invitation invitacion = invitacionQueCaducaEn(Duration.ofHours(48));

        invitacion.marcarComoUsada();

        assertThat(invitacion.estaUsada()).isTrue();
        assertThat(invitacion.esUtilizable()).isFalse();
    }
}
