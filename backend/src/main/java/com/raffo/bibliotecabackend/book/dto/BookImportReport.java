package com.raffo.bibliotecabackend.book.dto;

import java.util.List;

public record BookImportReport(
        int rowsRead,
        int booksCreated,
        int booksDiscarded,
        List<BookImportError> errors
) {
}
