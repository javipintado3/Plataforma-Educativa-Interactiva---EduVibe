package com.eduvibe.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrganizationTest {

    @Test
    @DisplayName("sin dominio configurado se admite cualquier email")
    void sinDominioTodoVale() {
        Organization centro = new Organization("Centro", null);

        assertThat(centro.admiteEmail("quien.sea@gmail.com")).isTrue();
    }

    @Test
    @DisplayName("con dominio configurado solo se admiten sus direcciones")
    void conDominioSoloEseDominio() {
        Organization centro = new Organization("Centro", "iesalixar.edu");

        assertThat(centro.admiteEmail("ana@iesalixar.edu")).isTrue();
        assertThat(centro.admiteEmail("ana@gmail.com")).isFalse();
    }

    @Test
    @DisplayName("la comprobación del dominio no distingue mayúsculas")
    void dominioSinDistinguirMayusculas() {
        Organization centro = new Organization("Centro", "IesAlixar.edu");

        assertThat(centro.admiteEmail("Ana@IESALIXAR.EDU")).isTrue();
    }

    @Test
    @DisplayName("un dominio que solo coincide al final no cuela")
    void noValeTerminarParecido() {
        Organization centro = new Organization("Centro", "alixar.edu");

        // "ana@falsoalixar.edu" termina en "alixar.edu", pero el dominio no es ese
        assertThat(centro.admiteEmail("ana@falsoalixar.edu")).isFalse();
    }
}
