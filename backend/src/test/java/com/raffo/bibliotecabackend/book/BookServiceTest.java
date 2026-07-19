package com.raffo.bibliotecabackend.book;

import com.raffo.bibliotecabackend.book.dto.BookRequest;
import com.raffo.bibliotecabackend.common.exception.ConflictException;
import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.data.jpa.domain.Specification;
import static org.mockito.ArgumentMatchers.any;

/*
 * Unit test del layer Service.
 *
 * Qui NON avviamo Spring e NON usiamo un database vero.
 * Testiamo BookService in isolamento, sostituendo BookRepository con un mock Mockito.
 *
 * Obiettivo:
 * - verificare la logica CRUD del service;
 * - verificare che il service chiami il repository corretto;
 * - verificare i casi di errore, come libro non trovato o ISBN duplicato.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    /*
     * @Mock crea un oggetto finto.
     *
     * bookRepository non parlera' con il database: nei test decidiamo noi
     * cosa deve restituire usando when(...).thenReturn(...).
     */
    @Mock
    private BookRepository bookRepository;

    /*
     * @InjectMocks crea il BookService reale e gli inietta dentro il mock
     * bookRepository. In questo modo testiamo codice vero del service, ma
     * controlliamo completamente la dipendenza esterna.
     */
    @InjectMocks
    private BookService bookService;

    /*
     * Read all.
     *
     * Il controller espone GET /api/catalog/books, ma a livello service la logica
     * corrispondente e' findAll().
     */
    @Test
    void findAllShouldReturnAllBooks() {
        // Arrange: preparo i dati finti e istruisco il repository mock.
        Book book1 = new Book("Clean Code", "Robert Martin", "ISBN-1", 2008, 3, 2);
        Book book2 = new Book("Effective Java", "Joshua Bloch", "ISBN-2", 2018, 4, 4);

        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));

        // Act: chiamo il metodo reale del service.
        List<Book> result = bookService.findAll();

        // Assert: controllo risultato e interazione col repository.
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(book1, book2);
        verify(bookRepository).findAll();
    }

    @Test
    void findByIdShouldReturnBookWhenExists() {
        // Arrange
        Book book = new Book("Clean Code", "Robert Martin", "ISBN-1", 2008, 3, 2);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act
        Book result = bookService.findById(1L);

        // Assert: isSameAs verifica che sia proprio la stessa istanza.
        assertThat(result).isSameAs(book);
        verify(bookRepository).findById(1L);
    }

    @Test
    void findByIdShouldThrowNotFoundWhenBookDoesNotExist() {
        // Arrange: Optional.empty simula un record assente nel database.
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert: ci aspettiamo una eccezione di dominio.
        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Libro non trovato");

        verify(bookRepository).findById(99L);
    }

    @Test
    void createShouldSaveBookWhenIsbnIsNotUsed() {
        // Arrange: il DTO rappresenta il body che arriverebbe dal controller.
        BookRequest request = new BookRequest(
                "Clean Code",
                "Robert Martin",
                "ISBN-1",
                2008,
                3,
                2
        );

        /*
         * Simuliamo che l'ISBN non sia gia' presente.
         * thenAnswer restituisce al test lo stesso Book passato a save(...),
         * come se il repository lo avesse salvato.
         */
        when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Book result = bookService.create(request);

        // Assert: controllo che il Book creato contenga i dati del request.
        assertThat(result.getTitle()).isEqualTo("Clean Code");
        assertThat(result.getAuthor()).isEqualTo("Robert Martin");
        assertThat(result.getIsbn()).isEqualTo("ISBN-1");
        assertThat(result.getPublicationYear()).isEqualTo(2008);
        assertThat(result.getTotalCopies()).isEqualTo(3);
        assertThat(result.getAvailableCopies()).isEqualTo(2);

        verify(bookRepository).existsByIsbn("ISBN-1");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createShouldThrowConflictWhenIsbnAlreadyExists() {
        // Arrange
        BookRequest request = new BookRequest(
                "Clean Code",
                "Robert Martin",
                "ISBN-1",
                2008,
                3,
                2
        );

        when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(true);

        // Act + Assert: ISBN duplicato significa errore di conflitto dati.
        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("ISBN gia' presente: esiste gia' un altro libro salvato con ISBN ISBN-1.");

        // Il salvataggio non deve essere chiamato se la validazione di dominio fallisce.
        verify(bookRepository).existsByIsbn("ISBN-1");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateShouldModifyBookWhenBookExistsAndIsbnIsAvailable() {
        // Arrange: existingBook simula l'entita' gia' presente nel database.
        Book existingBook = new Book("Old title", "Old author", "OLD-ISBN", 2000, 1, 1);

        BookRequest request = new BookRequest(
                "New title",
                "New author",
                "NEW-ISBN",
                2024,
                5,
                4
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbnAndIdNot("NEW-ISBN", 1L)).thenReturn(false);
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        // Act
        Book result = bookService.update(1L, request);

        // Assert: il service deve copiare i nuovi dati dentro l'entita' esistente.
        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getAuthor()).isEqualTo("New author");
        assertThat(result.getIsbn()).isEqualTo("NEW-ISBN");
        assertThat(result.getPublicationYear()).isEqualTo(2024);
        assertThat(result.getTotalCopies()).isEqualTo(5);
        assertThat(result.getAvailableCopies()).isEqualTo(4);

        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbnAndIdNot("NEW-ISBN", 1L);
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateShouldThrowConflictWhenIsbnBelongsToAnotherBook() {
        // Arrange
        Book existingBook = new Book("Old title", "Old author", "OLD-ISBN", 2000, 1, 1);

        BookRequest request = new BookRequest(
                "New title",
                "New author",
                "NEW-ISBN",
                2024,
                5,
                4
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbnAndIdNot("NEW-ISBN", 1L)).thenReturn(true);

        /*
         * Act + Assert.
         *
         * existsByIsbnAndIdNot(...) serve proprio a dire:
         * "questo ISBN e' usato da un libro diverso da quello che sto modificando?"
         */
        assertThatThrownBy(() -> bookService.update(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("ISBN gia' presente: esiste gia' un altro libro salvato con ISBN NEW-ISBN.");

        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbnAndIdNot("NEW-ISBN", 1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteShouldRemoveBookWhenExists() {
        // Arrange: prima di eliminare, il service deve trovare il libro.
        Book book = new Book("Clean Code", "Robert Martin", "ISBN-1", 2008, 3, 2);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act
        bookService.delete(1L);

        // Assert: viene eliminata esattamente l'entita' trovata.
        verify(bookRepository).findById(1L);
        verify(bookRepository).delete(book);
    }

    @Test
    void deleteShouldThrowNotFoundWhenBookDoesNotExist() {
        // Arrange
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert: non posso eliminare un libro inesistente.
        assertThatThrownBy(() -> bookService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Libro non trovato");

        // delete(...) non deve essere chiamato se findById fallisce.
        verify(bookRepository).findById(99L);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void searchShouldUseSpecificationWhenFiltersAreProvided() {
        Book book = new Book("Dune", "Frank Herbert", "12345", 1965, 3, 2);

        when(bookRepository.findAll(any(Specification.class))).thenReturn(List.of(book));

        List<Book> result = bookService.search("dune", "herbert", null, 1965);

        assertThat(result).containsExactly(book);
        verify(bookRepository).findAll(any(Specification.class));
    }


}
