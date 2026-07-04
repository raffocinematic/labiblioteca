package com.raffo.bibliotecabackend.book;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    /**
     * In modifica, devo controllare se l'ISBN è già usato da un altro libro,
     * ignorando il libro che stai modificando
     *
     */
    boolean existsByIsbnAndIdNot(String isbn, Long id);
}
