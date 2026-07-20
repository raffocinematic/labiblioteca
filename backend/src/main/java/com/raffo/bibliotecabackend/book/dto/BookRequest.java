package com.raffo.bibliotecabackend.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;

public record BookRequest(
        @NotBlank
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Il titolo puo' contenere solo lettere e spazi")
        String title,

        @NotBlank
        @Pattern(regexp = "^[\\p{L} ]+$", message = "L'autore puo' contenere solo lettere e spazi")
        String author,

        @NotBlank
        @Pattern(regexp = "^\\d+$", message = "L'ISBN puo' contenere solo numeri")
        String isbn,

        @Min(0)
        @Max(9999)
        Integer publicationYear,

        @NotNull @Min(0) Integer totalCopies,
        @NotNull @Min(0) Integer availableCopies
) {

    @AssertTrue(message = "Le copie disponibili non possono essere maggiori delle copie totali")
    public boolean isAvailableCopiesLessThanOrEqualToTotalCopies() {
        if (totalCopies == null || availableCopies == null) {
            return true;
        }
        return availableCopies <= totalCopies;
    }
}
