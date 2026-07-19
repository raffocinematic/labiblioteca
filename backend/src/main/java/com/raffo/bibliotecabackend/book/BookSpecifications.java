package com.raffo.bibliotecabackend.book;

import org.springframework.data.jpa.domain.Specification;

/**
 * Separiamo la logica dei singoli filtri dal service.
 *
 * Ogni metodo rappresenta un pezzo di WHERE.
 *
 */
public final class BookSpecifications {

    private BookSpecifications() {
    }

    public static Specification<Book> titleContains(String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                );
    }

    public static Specification<Book> authorContains(String author) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")),
                        "%" + author.toLowerCase() + "%"
                );
    }

    public static Specification<Book> isbnContains(String isbn) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("isbn"), "%" + isbn + "%");
    }

    public static Specification<Book> publicationYearEquals(Integer publicationYear) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("publicationYear"), publicationYear);
    }
}
