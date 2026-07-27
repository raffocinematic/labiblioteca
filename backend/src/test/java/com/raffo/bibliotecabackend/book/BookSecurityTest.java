package com.raffo.bibliotecabackend.book;

import com.raffo.bibliotecabackend.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.mockito.ArgumentMatchers.any;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffo.bibliotecabackend.book.dto.BookRequest;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.Mockito.verify;


/**
 * Qui verifichiamo 3 casi
 *
 * GET /api/catalog/books senza token -> 401
 * GET /api/catalog/books con token valido -> 200
 * GET /api/catalog/books con token non valido -> 401
 *
 */

@SpringBootTest
@AutoConfigureMockMvc
class BookSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // book service serve al controller
    @MockitoBean
    private BookService bookService;

    // jwt service serve al filtro
    @MockitoBean
    private JwtService jwtService;

    // user details service serve al filtro per caricare l'utente
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test senza token
     */
    @Test
    void getBooksWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/catalog/books"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test con token valido
     * NOn stiamo testando JJWT reale : testiamo che il filtro, quando JwtService dice "token valido", popola
     * il SecurityContext e lascia passare la request.
     */
    @Test
    void getBooksWithValidTokenShouldReturn200() throws Exception {
        UserDetails userDetails = User.builder()
                .username("raffo")
                .password("password-hash")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractUsername("valid-token")).thenReturn("raffo");
        when(userDetailsService.loadUserByUsername("raffo")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);
        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/catalog/books")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test con token INVALIDO
     */
    @Test
    void getBooksWithInvalidTokenShouldReturn401() throws Exception {
        when(jwtService.extractUsername("bad-token"))
                .thenThrow(new JwtException("Token non valido"));

        mockMvc.perform(get("/api/catalog/books")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBookWithUserRoleShouldReturn403() throws Exception {
        authenticateAs("user-token", "mario", "ROLE_USER");

        BookRequest request = validBookRequest();

        mockMvc.perform(post("/api/catalog/books")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBookWithAdminRoleShouldReturn201() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        BookRequest request = validBookRequest();

        Book createdBook = new Book(
                request.title(),
                request.author(),
                request.isbn(),
                request.genre(),
                request.publicationYear(),
                request.totalCopies(),
                request.availableCopies()
        );

        when(bookService.create(any(BookRequest.class))).thenReturn(createdBook);

        mockMvc.perform(post("/api/catalog/books")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"));
    }

    @Test
    void updateBookWithUserRoleShouldReturn403() throws Exception {
        authenticateAs("user-token", "mario", "ROLE_USER");

        BookRequest request = validBookRequest();

        mockMvc.perform(put("/api/catalog/books/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBookWithAdminRoleShouldReturn200() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        BookRequest request = validBookRequest();

        Book updatedBook = new Book(
                request.title(),
                request.author(),
                request.isbn(),
                request.genre(),
                request.publicationYear(),
                request.totalCopies(),
                request.availableCopies()
        );

        when(bookService.update(eq(1L), any(BookRequest.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/api/catalog/books/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void deleteBookWithUserRoleShouldReturn403() throws Exception {
        authenticateAs("user-token", "mario", "ROLE_USER");

        mockMvc.perform(delete("/api/catalog/books/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBookWithAdminRoleShouldReturn204() throws Exception {
        authenticateAs("admin-token", "admin", "ROLE_ADMIN");

        mockMvc.perform(delete("/api/catalog/books/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(bookService).delete(1L);
    }

    // -------------------------------------------------------------------------------------------------------

    private void authenticateAs(String token, String username, String role) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password-hash")
                .authorities(role)
                .build();

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);
    }

    private BookRequest validBookRequest() {
        return new BookRequest(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                BookGenre.TECNICO,
                2008,
                3,
                3
        );
    }

}
