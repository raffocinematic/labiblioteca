package com.raffo.bibliotecabackend.book;

import com.raffo.bibliotecabackend.book.dto.BookRequest;
import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.raffo.bibliotecabackend.common.exception.ConflictException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.raffo.bibliotecabackend.common.exception.BadRequestException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Pageable contiene page, size, sort. Il repo JpaRepository sa già usarlo e non devi scrivere query custom.
     */
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Libro non trovato: " + id));
    }

    @Transactional
    public Book create(BookRequest request) {

        validateCopies(request);

        if(bookRepository.existsByIsbn(request.isbn())) {
            throw new ConflictException("ISBN gia' presente: esiste gia' un altro libro salvato con ISBN " + request.isbn() + ".");
        }

        Book book = new Book(
                request.title(),
                request.author(),
                request.isbn(),
                request.publicationYear(),
                request.totalCopies(),
                request.availableCopies()
        );

        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, BookRequest request) {
        validateCopies(request);
        Book book = findById(id);

        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new ConflictException("ISBN gia' presente: esiste gia' un altro libro salvato con ISBN " + request.isbn() + ".");
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPublicationYear(request.publicationYear());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());

        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = findById(id);
        bookRepository.delete(book);
    }

    public List<Book> search(String title, String author, String isbn, Integer publicationYear) {
        Specification<Book> specification = Specification.unrestricted();

        if (title != null && !title.isBlank()) {
            specification = specification.and(BookSpecifications.titleContains(title.trim()));
        }

        if (author != null && !author.isBlank()) {
            specification = specification.and(BookSpecifications.authorContains(author.trim()));
        }

        if (isbn != null && !isbn.isBlank()) {
            specification = specification.and(BookSpecifications.isbnContains(isbn.trim()));
        }

        if (publicationYear != null) {
            specification = specification.and(BookSpecifications.publicationYearEquals(publicationYear));
        }

        return bookRepository.findAll(specification);
    }

    private void validateCopies(BookRequest request) {
        if (request.availableCopies() > request.totalCopies()) {
            throw new BadRequestException("Le copie disponibili non possono essere maggiori delle copie totali.");
        }
    }
}
