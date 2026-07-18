package com.raffo.bibliotecabackend.book;

import com.raffo.bibliotecabackend.auth.JwtAuthenticationEntryPoint;
import com.raffo.bibliotecabackend.auth.JwtAuthenticationFilter;
import com.raffo.bibliotecabackend.auth.JwtService;
import com.raffo.bibliotecabackend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Qui verifichiamo 3 casi
 *
 * GET /api/catalog/books senza token -> 401
 * GET /api/catalog/books con token valido -> 200
 * GET /api/catalog/books con token non valido -> 401
 *
 */

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class
})
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
        when(bookService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/catalog/books")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
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

}
