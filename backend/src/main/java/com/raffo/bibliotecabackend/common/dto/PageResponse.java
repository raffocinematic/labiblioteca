package com.raffo.bibliotecabackend.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;


public record PageResponse<T> (
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Page<T> è un tipo di Spring pieno di metadati tecnici. Il FE non ha bisogno di tutto: gli basta
     * content, pagina corrente, size e totali.
     *
     * Il generic <T> ti permette di riusare questo DTO per libri, utenti, prestiti, ecc.
     *
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<> (
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}