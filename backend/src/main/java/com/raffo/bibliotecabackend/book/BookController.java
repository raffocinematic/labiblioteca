package com.raffo.bibliotecabackend.book;

import com.raffo.bibliotecabackend.book.dto.BookRequest;
import com.raffo.bibliotecabackend.book.dto.BookResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {

        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> getBooks() {
        return bookService.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping("/search")
    public List<BookResponse> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer publicationYear
    ) {
        return bookService.search(title, author, isbn, publicationYear).stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public BookResponse getBook(@PathVariable Long id) {
        return BookResponse.from(bookService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteBook(@PathVariable Long id) {
            bookService.delete(id);
        }

}
