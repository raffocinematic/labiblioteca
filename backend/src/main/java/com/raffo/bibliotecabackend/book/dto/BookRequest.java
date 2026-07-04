package com.raffo.bibliotecabackend.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String isbn,
        Integer publicationYear,
        @NotNull @Min(0) Integer totalCopies,
        @NotNull @Min(0) Integer availableCopies
) {
}
