package com.raffo.bibliotecabackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffo.bibliotecabackend.auth.dto.RegisterRequest;
import com.raffo.bibliotecabackend.common.exception.BadRequestException;
import com.raffo.bibliotecabackend.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.raffo.bibliotecabackend.auth.JwtAuthenticationFilter;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    /**
     * Questo test non testa la logica "password diverse" dentro AuthService, quella è responsabilità di un service test.
     * Qui stai testando che, quando il controller riceva la tua BadRequestException, il GlobalExceptionHandler la trasformi davvero in HTTP 400.
     *
     * @throws Exception
     */
    @Test
    void registerWithDifferentPasswords_shouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "mario",
                "password123",
                "different123"
        );

        when(authService.register(request))
                .thenThrow(new BadRequestException("Passwords don't match"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Passwords don't match"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

        verify(authService).register(request);
    }

}
