package com.raffo.bibliotecabackend.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffo.bibliotecabackend.book.dto.BookRequest;
import com.raffo.bibliotecabackend.common.exception.ConflictException;
import com.raffo.bibliotecabackend.common.exception.GlobalExceptionHandler;
import com.raffo.bibliotecabackend.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Test del layer Controller.
 *
 * Qui testiamo il comportamento HTTP di BookController:
 * - metodo HTTP e URL;
 * - status code;
 * - JSON restituito;
 * - validazione del request body;
 * - traduzione delle eccezioni in risposte HTTP tramite GlobalExceptionHandler.
 *
 * Non testiamo la logica di business del service: quella e' coperta da BookServiceTest.
 */
@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    /*
     * MockMvc simula chiamate HTTP al controller senza avviare Tomcat
     * e senza far partire l'applicazione completa.
     */
    @Autowired
    private MockMvc mockMvc;

    /*
     * ObjectMapper converte oggetti Java in JSON.
     *
     * Lo creiamo direttamente nel test invece di chiederlo a Spring, perche'
     * questo @WebMvcTest carica solo una porzione molto stretta del contesto MVC.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * @MockitoBean registra nel contesto Spring un mock Mockito.
     *
     * BookController dipende da BookService: in questo test non vogliamo usare
     * il service reale, quindi lo sostituiamo con un mock controllato dal test.
     */
    @MockitoBean
    private BookService bookService;

    /*
     * Helper per creare un Book con id.
     *
     * Nel dominio l'id non ha setter pubblico perche' normalmente viene generato
     * da JPA. Nei test del controller ci serve un id per verificare il JSON,
     * quindi usiamo ReflectionTestUtils solo in ambiente di test.
     */
    private Book bookWithId(Long id) {
        Book book = new Book("Clean Code", "Robert Martin", "ISBN-1", 2008, 3, 2);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    /*
     * GET /api/catalog/books.
     *
     * Il service restituisce due libri e il controller deve rispondere 200
     * con un array JSON di due elementi.
     */
    @Test
    void getBooksShouldReturnBooks() throws Exception {
        // Arrange
        when(bookService.findAll()).thenReturn(List.of(bookWithId(1L), bookWithId(2L)));

        // Act + Assert
        mockMvc.perform(get("/api/catalog/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));

        // Verifico che il controller abbia delegato al service.
        verify(bookService).findAll();
    }

    @Test
    void getBookShouldReturnBook() throws Exception {
        // Arrange
        when(bookService.findById(1L)).thenReturn(bookWithId(1L));

        // Act + Assert: GET /api/catalog/books/{id} deve restituire il DTO del libro.
        mockMvc.perform(get("/api/catalog/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.isbn").value("ISBN-1"));

        verify(bookService).findById(1L);
    }

    @Test
    void getBookShouldReturn404WhenBookDoesNotExist() throws Exception {
        /*
         * Arrange.
         * Simuliamo il service che lancia NotFoundException.
         * GlobalExceptionHandler deve trasformarla in HTTP 404.
         */
        when(bookService.findById(99L)).thenThrow(new NotFoundException("Libro non trovato: 99"));

        // Act + Assert
        mockMvc.perform(get("/api/catalog/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Libro non trovato: 99"))
                .andExpect(jsonPath("$.path").value("/api/catalog/books/99"));

        verify(bookService).findById(99L);
    }

    @Test
    void createBookShouldReturn201AndCreatedBook() throws Exception {
        // Arrange: request rappresenta il JSON che il client mandera' nel body.
        BookRequest request = new BookRequest("Clean Code", "Robert Martin", "ISBN-1", 2008, 3, 2);

        when(bookService.create(request)).thenReturn(bookWithId(1L));

        /*
         * Act + Assert.
         * contentType("application/json") dice a Spring di leggere il body come JSON.
         * objectMapper.writeValueAsString(request) converte il DTO Java in JSON.
         */
        mockMvc.perform(post("/api/catalog/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("ISBN-1"));

        verify(bookService).create(request);
    }

    @Test
    void createBookShouldReturn400WhenRequestIsInvalid() throws Exception {
        /*
         * Arrange.
         * Questo request viola le annotazioni di validazione in BookRequest:
         * @NotBlank per title/author/isbn e @NotNull per totalCopies/availableCopies.
         */
        BookRequest request = new BookRequest("", "", "", null, null, null);

        /*
         * Act + Assert.
         * Il controller non deve arrivare al service: @Valid blocca prima la request
         * e GlobalExceptionHandler restituisce un JSON con validationErrors.
         */
        mockMvc.perform(post("/api/catalog/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.author").exists())
                .andExpect(jsonPath("$.validationErrors.isbn").exists())
                .andExpect(jsonPath("$.validationErrors.totalCopies").exists())
                .andExpect(jsonPath("$.validationErrors.availableCopies").exists());
    }

    @Test
    void updateBookShouldReturnUpdatedBook() throws Exception {
        // Arrange
        BookRequest request = new BookRequest("Updated title", "Updated author", "ISBN-2", 2024, 5, 4);

        Book updatedBook = new Book("Updated title", "Updated author", "ISBN-2", 2024, 5, 4);
        ReflectionTestUtils.setField(updatedBook, "id", 1L);

        when(bookService.update(1L, request)).thenReturn(updatedBook);

        // Act + Assert: PUT /api/catalog/books/{id} deve rispondere 200 con il libro aggiornato.
        mockMvc.perform(put("/api/catalog/books/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.isbn").value("ISBN-2"));

        verify(bookService).update(1L, request);
    }

    @Test
    void updateBookShouldReturn409WhenIsbnAlreadyExists() throws Exception {
        // Arrange
        BookRequest request = new BookRequest("Updated title", "Updated author", "ISBN-2", 2024, 5, 4);

        /*
         * Simuliamo una regola di business fallita nel service.
         * Il controller non gestisce direttamente l'errore: lo fa GlobalExceptionHandler.
         */
        when(bookService.update(1L, request))
                .thenThrow(new ConflictException("Esiste gia' un altro libro con ISBN: ISBN-2"));

        // Act + Assert: ConflictException deve diventare HTTP 409.
        mockMvc.perform(put("/api/catalog/books/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Esiste gia' un altro libro con ISBN: ISBN-2"));

        verify(bookService).update(1L, request);
    }

    @Test
    void deleteBookShouldReturn204() throws Exception {
        /*
         * DELETE riuscita.
         * Il controller ha @ResponseStatus(HttpStatus.NO_CONTENT), quindi
         * ci aspettiamo 204 e nessun body.
         */
        mockMvc.perform(delete("/api/catalog/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).delete(1L);
    }

    @Test
    void deleteBookShouldReturn404WhenBookDoesNotExist() throws Exception {
        /*
         * doThrow(...).when(...) si usa spesso con metodi void.
         * bookService.delete(...) non restituisce nulla, quindi non possiamo usare when(...).thenThrow(...).
         */
        doThrow(new NotFoundException("Libro non trovato: 99"))
                .when(bookService).delete(99L);

        // Act + Assert: anche su DELETE, NotFoundException deve diventare 404.
        mockMvc.perform(delete("/api/catalog/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Libro non trovato: 99"));

        verify(bookService).delete(99L);
    }
}
