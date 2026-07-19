package com.raffo.bibliotecabackend.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

//JpaSpecificExecutor permette di costruire query dinamiche, se arriva title filtri per titolo; se arriva anche
//author, aggiunge anche autore. Se un paramento manca, non lo consideri.
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);

    /**
     * In modifica, devo controllare se l'ISBN è già usato da un altro libro,
     * ignorando il libro che stai modificando
     *
     */
    boolean existsByIsbnAndIdNot(String isbn, Long id);
}
