package com.raffo.bibliotecabackend.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 255)
        String author,

        @NotBlank
        @ValidIsbn
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
