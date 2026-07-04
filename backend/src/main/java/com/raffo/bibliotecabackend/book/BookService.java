package com.raffo.bibliotecabackend.book;

import com.raffo.bibliotecabackend.book.dto.BookRequest;
import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.raffo.bibliotecabackend.common.exception.ConflictException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Libro non trovato: " + id));
    }

    @Transactional
    public Book create(BookRequest request) {

        if(bookRepository.existsByIsbn(request.isbn())) {
            throw new ConflictException("Esiste già un libro con ISBN: " + request.isbn());
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
        Book book = findById(id);

        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new ConflictException("Esiste già un altro libro con ISBN: " + request.isbn());
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
}
