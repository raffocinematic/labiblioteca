package com.raffo.bibliotecabackend.book.dto;

public record BookImportError(
        int rowNumber,
        String message
) {
}
