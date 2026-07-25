package com.raffo.bibliotecabackend.book.dto;

import com.raffo.bibliotecabackend.book.Book;
import com.raffo.bibliotecabackend.book.BookGenre;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BookGenre genre,
        Integer publicationYear,
        Integer totalCopies,
        Integer availableCopies

) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getGenre(),
                book.getPublicationYear(),
                book.getTotalCopies(),
                book.getAvailableCopies()
        );
    }
}
