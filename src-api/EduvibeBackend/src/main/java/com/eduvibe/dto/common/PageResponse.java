package com.eduvibe.dto.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Envoltorio de paginación propio.
 *
 * Se usa en lugar de devolver el Page de Spring Data directamente porque la
 * serialización de este último no es estable entre versiones, y expone detalles
 * internos (pageable, sort...) que el cliente no necesita.
 */
public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean ultima) {

    /**
     * Cuando el contenido ya está construido a parte (porque montarlo necesita
     * consultas para toda la página a la vez, no una por elemento) y solo falta
     * envolverlo con los datos de paginación.
     */
    public static <T> PageResponse<T> de(Page<?> page, List<T> contenido) {
        return new PageResponse<>(
                contenido,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    public static <E, T> PageResponse<T> de(Page<E> page, Function<E, T> mapeador) {
        return new PageResponse<>(
                page.getContent().stream().map(mapeador).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
