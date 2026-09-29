package com.eduvibe.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.eduvibe.dto.common.PageResponse;

class PaginacionTest {

    @Test
    @DisplayName("Conserva la página y el tamaño pedidos y descarta el orden")
    void keepsPageAndSizeButDropsSort() {
        Pageable pedida = PageRequest.of(3, 7, Sort.by(Sort.Direction.DESC, "loQueSea"));

        Pageable limpia = Paginacion.sinOrden(pedida);

        assertThat(limpia.getPageNumber()).isEqualTo(3);
        assertThat(limpia.getPageSize()).isEqualTo(7);
        assertThat(limpia.getSort().isUnsorted()).isTrue();
    }

    @Test
    @DisplayName("Sin texto de búsqueda el patrón casa con todo")
    void blankSearchMatchesEverything() {
        assertThat(Paginacion.patronDeBusqueda(null)).isEqualTo("%");
        assertThat(Paginacion.patronDeBusqueda("")).isEqualTo("%");
        assertThat(Paginacion.patronDeBusqueda("   ")).isEqualTo("%");
    }

    @Test
    @DisplayName("El texto de búsqueda se recorta y pasa a minúsculas dentro de un LIKE")
    void searchTextIsTrimmedAndLowercased() {
        assertThat(Paginacion.patronDeBusqueda("  Matemáticas ")).isEqualTo("%matemáticas%");
    }

    @Test
    @DisplayName("El envoltorio con contenido ya construido conserva los datos de paginación")
    void wrapsPrebuiltContentKeepingPagingData() {
        var pagina = new PageImpl<>(List.of("a", "b"), PageRequest.of(2, 2), 6);

        PageResponse<Integer> respuesta = PageResponse.de(pagina, List.of(1, 2));

        assertThat(respuesta.contenido()).containsExactly(1, 2);
        assertThat(respuesta.pagina()).isEqualTo(2);
        assertThat(respuesta.tamano()).isEqualTo(2);
        assertThat(respuesta.totalElementos()).isEqualTo(6);
        assertThat(respuesta.totalPaginas()).isEqualTo(3);
        assertThat(respuesta.ultima()).isTrue();
    }
}
