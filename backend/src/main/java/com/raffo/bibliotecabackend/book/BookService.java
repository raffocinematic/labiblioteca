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
import com.raffo.bibliotecabackend.audit.AuditAction;
import com.raffo.bibliotecabackend.audit.AuditEventPublisher;

import java.util.Map;
import java.util.List;

import com.raffo.bibliotecabackend.book.dto.BookImportError;
import com.raffo.bibliotecabackend.book.dto.BookImportReport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    // Book service deve poter pubblicare eventi audit ma senza conoscere repository o tabella audit
    private final AuditEventPublisher auditEventPublisher;
    private final Validator validator;

    public BookService(BookRepository bookRepository,
                       AuditEventPublisher auditEventPublisher,
                       Validator validator) {
        this.bookRepository = bookRepository;
        this.auditEventPublisher = auditEventPublisher;
        this.validator = validator;
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
        // !!!
        String isbn = IsbnUtils.normalize(request.isbn());

        if(bookRepository.existsByIsbn(isbn)) {
            throw new ConflictException("ISBN gia' presente: esiste gia' un altro libro salvato con ISBN " + isbn + ".");
        }

        Book book = new Book(
                request.title(),
                request.author(),
                isbn,
                request.genre(),
                request.publicationYear(),
                request.totalCopies(),
                request.availableCopies()
        );

        // Prima salvi il libro, poi hai l'Id generato dal DB, poi pubblichi l'evento.
        Book savedBook = bookRepository.save(book);

        auditEventPublisher.publish(
                AuditAction.BOOK_CREATED,
                "Book",
                savedBook.getId(),
                Map.of(
                        "title", savedBook.getTitle(),
                        "isbn", savedBook.getIsbn()
                )
        );

        return savedBook;
    }

    /**
     * Diamo al metodo una sequenza pulita: validazione/normalizzazione input
     * recupero stati esistente
     * controlli business
     * mutazione entità
     * save
     *
     * @param id
     * @param request
     * @return
     */
    @Transactional
    public Book update(Long id, BookRequest request) {

        validateCopies(request);

        // come in create: normalizzi 1 volta e poi usi la variabile isbn
        String isbn = IsbnUtils.normalize(request.isbn());

        Book book = findById(id);

        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ConflictException("ISBN gia' presente: esiste gia' un altro libro salvato con ISBN "
                    + isbn + ".");
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(isbn);
        book.setGenre(request.genre());
        book.setPublicationYear(request.publicationYear());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());

        Book savedBook = bookRepository.save(book);

        auditEventPublisher.publish(
                AuditAction.BOOK_UPDATED,
                "Book",
                savedBook.getId(),
                Map.of(
                        "title", savedBook.getTitle(),
                        "isbn", savedBook.getIsbn()
                )
        );

        return savedBook;
    }

    @Transactional
    public void delete(Long id) {
        Book book = findById(id);

        String title = book.getTitle();
        String isbn = book.getIsbn();

        bookRepository.delete(book);

        auditEventPublisher.publish(
                AuditAction.BOOK_DELETED,
                "Book",
                id,
                Map.of(
                        "title", title,
                        "isbn", isbn
                )
        );
    }

    public List<Book> search(String title, String author, String isbn, BookGenre genre, Integer publicationYear) {
        Specification<Book> specification = Specification.unrestricted();

        if (title != null && !title.isBlank()) {
            specification = specification.and(BookSpecifications.titleContains(title.trim()));
        }

        if (author != null && !author.isBlank()) {
            specification = specification.and(BookSpecifications.authorContains(author.trim()));
        }

        if (isbn != null && !isbn.isBlank()) {
            specification = specification.and(BookSpecifications.isbnContains(IsbnUtils.normalize(isbn)));
        }

        if (genre != null) {
            specification = specification.and(BookSpecifications.genreEquals(genre));
        }

        if (publicationYear != null) {
            //perché se nel DB salvi 9483812431, una ricerca con 948-38-123431 deve comunque trovare il libro.
            specification = specification.and(BookSpecifications.publicationYearEquals(publicationYear));
        }

        return bookRepository.findAll(specification);
    }

    @Transactional
    public BookImportReport importFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Il file CSV e' obbligatorio.");
        }

        int rowsRead = 0;
        int booksCreated = 0;

        Map<String, BookRequest> requestsByIsbn = new LinkedHashMap<>();
        Map<String, Integer> rowByIsbn = new LinkedHashMap<>();
        List<BookImportError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String header = reader.readLine();

            if (header == null || header.isBlank()) {
                throw new BadRequestException("Il file CSV e' vuoto.");
            }

            validateCsvHeader(header);

            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (line.isBlank()) {
                    continue;
                }

                rowsRead++;

                try {
                    BookRequest request = parseCsvLine(line);
                    Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);

                    if (!violations.isEmpty()) {
                        errors.add(new BookImportError(rowNumber, formatValidationErrors(violations)));
                        continue;
                    }

                    String normalizedIsbn = IsbnUtils.normalize(request.isbn());

                    if (requestsByIsbn.containsKey(normalizedIsbn)) {
                        errors.add(new BookImportError(
                                rowNumber,
                                "ISBN duplicato nel file: " + normalizedIsbn
                                        + " gia' presente alla riga " + rowByIsbn.get(normalizedIsbn)
                        ));
                        continue;
                    }

                    requestsByIsbn.put(normalizedIsbn, request);
                    rowByIsbn.put(normalizedIsbn, rowNumber);

                } catch (IllegalArgumentException exception) {
                    errors.add(new BookImportError(rowNumber, exception.getMessage()));
                }
            }

        } catch (IOException exception) {
            throw new BadRequestException("Impossibile leggere il file CSV.");
        }

        for (Map.Entry<String, BookRequest> entry : requestsByIsbn.entrySet()) {
            try {
                create(entry.getValue());
                booksCreated++;
            } catch (BadRequestException | ConflictException exception) {
                errors.add(new BookImportError(rowByIsbn.get(entry.getKey()), exception.getMessage()));
            }
        }

        return new BookImportReport(
                rowsRead,
                booksCreated,
                errors.size(),
                errors
        );
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void validateCopies(BookRequest request) {

        if (request.availableCopies() > request.totalCopies()) {
            throw new BadRequestException("Le copie disponibili non possono essere maggiori delle copie totali.");
        }
    }

    private void validateCsvHeader(String header) {
        String expected = "title,author,isbn,genre,publicationYear,totalCopies,availableCopies";

        if (!expected.equals(header.trim())) {
            throw new BadRequestException("Header CSV non valido. Header atteso: " + expected);
        }
    }

    private BookRequest parseCsvLine(String line) {
        String[] columns = line.split(",", -1);

        if (columns.length != 7) {
            throw new IllegalArgumentException("Numero colonne non valido: attese 7 colonne.");
        }

        return new BookRequest(
                required(columns[0], "title"),
                required(columns[1], "author"),
                required(columns[2], "isbn"),
                parseGenre(columns[3]),
                parseOptionalInteger(columns[4], "publicationYear"),
                parseRequiredInteger(columns[5], "totalCopies"),
                parseRequiredInteger(columns[6], "availableCopies")
        );
    }

    private String required(String value, String fieldName) {
        String trimmed = value.trim();

        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Campo obbligatorio mancante: " + fieldName);
        }

        return trimmed;
    }

    private BookGenre parseGenre(String value) {
        try {
            return BookGenre.valueOf(required(value, "genre").toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Genere non valido: " + value);
        }
    }

    private Integer parseOptionalInteger(String value, String fieldName) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return parseInteger(value, fieldName);
    }

    private Integer parseRequiredInteger(String value, String fieldName) {
        return parseInteger(required(value, fieldName), fieldName);
    }

    private Integer parseInteger(String value, String fieldName) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Campo numerico non valido: " + fieldName);
        }
    }

    private String formatValidationErrors(Set<ConstraintViolation<BookRequest>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList()
                .toString();
    }
}
